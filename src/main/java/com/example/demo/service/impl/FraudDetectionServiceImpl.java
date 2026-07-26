package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.events.*;
import com.example.demo.exception.FraudRuleNotFoundException;
import com.example.demo.metrics.BusinessMetricsService;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.FraudRuleRepository;
import com.example.demo.service.FraudDetectionService;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Enterprise Production-grade Fraud Detection and Risk Engine implementation.
 */
@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionServiceImpl.class);

    private static final String VELOCITY_PREFIX = "fraud:velocity:";
    private static final String PIN_FAIL_PREFIX = "fraud:pin_failures:";
    private static final String BLACKLIST_UPI_KEY = "fraud:blacklist:upi";
    private static final String BLACKLIST_DEVICE_KEY = "fraud:blacklist:device";

    private final FraudRuleRepository fraudRuleRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCacheService redisCacheService;
    private final OutboxService outboxService;
    private final EventPublisher eventPublisher;
    private final BusinessMetricsService businessMetricsService;

    // In-memory thread-safe cache for evaluation logs history
    private final List<FraudLogResponse> fraudAuditLogs = Collections.synchronizedList(new ArrayList<>());

    public FraudDetectionServiceImpl(FraudRuleRepository fraudRuleRepository,
                                     RedisTemplate<String, Object> redisTemplate,
                                     RedisCacheService redisCacheService,
                                     OutboxService outboxService,
                                     EventPublisher eventPublisher,
                                     BusinessMetricsService businessMetricsService) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.redisTemplate = redisTemplate;
        this.redisCacheService = redisCacheService;
        this.outboxService = outboxService;
        this.eventPublisher = eventPublisher;
        this.businessMetricsService = businessMetricsService;
    }

    @Override
    @Transactional(readOnly = true)
    public FraudEvaluationResult evaluateTransaction(FraudEvaluationRequest request) {
        log.info("Evaluating transaction risk for payer [{}] to payee [{}] amount [{}]",
                request.getPayerUpiId(), request.getPayeeUpiId(), request.getAmount());

        List<FraudRule> rules = fraudRuleRepository.findByEnabledTrueOrderByPriorityAsc();
        int score = 0;
        List<String> triggeredRules = new ArrayList<>();

        for (FraudRule rule : rules) {
            switch (rule.getType()) {
                case HIGH_VALUE_TRANSACTION:
                    BigDecimal threshold = rule.getThresholdAmount() != null ? rule.getThresholdAmount() : new BigDecimal("50000.00");
                    if (request.getAmount() != null && request.getAmount().compareTo(threshold) >= 0) {
                        score += 35;
                        triggeredRules.add(rule.getRuleName() + " (Amount >= " + threshold + ")");
                    }
                    break;

                case RAPID_TRANSACTIONS:
                case VELOCITY_LIMIT:
                    int timeWindow = rule.getTimeWindowMinutes() != null ? rule.getTimeWindowMinutes() : 5;
                    int maxCount = rule.getThresholdCount() != null ? rule.getThresholdCount() : 5;
                    String velocityKey = VELOCITY_PREFIX + request.getPayerUpiId();

                    Long currentVelocity = incrementRedisCounter(velocityKey, timeWindow);
                    if (currentVelocity != null && currentVelocity > maxCount) {
                        score += 40;
                        triggeredRules.add(rule.getRuleName() + " (" + currentVelocity + " txns in " + timeWindow + "m)");
                    }
                    break;

                case MULTIPLE_FAILED_PIN:
                    int pinLimit = rule.getThresholdCount() != null ? rule.getThresholdCount() : 3;
                    String pinKey = PIN_FAIL_PREFIX + request.getPayerUpiId();
                    Object pinFailObj = redisTemplate.opsForValue().get(pinKey);
                    int pinFails = pinFailObj != null ? Integer.parseInt(pinFailObj.toString()) : 0;
                    if (pinFails >= pinLimit) {
                        score += 50;
                        triggeredRules.add(rule.getRuleName() + " (" + pinFails + " PIN failures)");
                    }
                    break;

                case BLACKLISTED_UPI:
                    Boolean isPayerBlacklisted = redisTemplate.opsForSet().isMember(BLACKLIST_UPI_KEY, request.getPayerUpiId());
                    Boolean isPayeeBlacklisted = redisTemplate.opsForSet().isMember(BLACKLIST_UPI_KEY, request.getPayeeUpiId());
                    if (Boolean.TRUE.equals(isPayerBlacklisted) || Boolean.TRUE.equals(isPayeeBlacklisted)) {
                        score += 100;
                        triggeredRules.add(rule.getRuleName() + " (Blacklisted UPI ID)");
                    }
                    break;

                case BLACKLISTED_DEVICE:
                    if (request.getDeviceId() != null) {
                        Boolean isDeviceBlacklisted = redisTemplate.opsForSet().isMember(BLACKLIST_DEVICE_KEY, request.getDeviceId());
                        if (Boolean.TRUE.equals(isDeviceBlacklisted)) {
                            score += 100;
                            triggeredRules.add(rule.getRuleName() + " (Blacklisted Device)");
                        }
                    }
                    break;

                default:
                    break;
            }
        }

        int finalRiskScore = Math.min(100, score);
        FraudDecision decision;
        if (finalRiskScore >= 70) {
            decision = FraudDecision.BLOCK;
        } else if (finalRiskScore >= 30) {
            decision = FraudDecision.REVIEW;
        } else {
            decision = FraudDecision.ALLOW;
        }

        LocalDateTime evalTime = LocalDateTime.now();
        FraudEvaluationResult result = new FraudEvaluationResult(finalRiskScore, decision, triggeredRules, evalTime);

        // Record log audit entry
        FraudLogResponse logEntry = new FraudLogResponse(
                UUID.randomUUID(), request.getPayerUpiId(), request.getPayeeUpiId(),
                request.getAmount(), finalRiskScore, decision, triggeredRules, evalTime
        );
        fraudAuditLogs.add(0, logEntry);

        // Publish events and save to Outbox
        String correlationId = UUID.randomUUID().toString();
        long hashId = (long) Math.abs(request.getPayerUpiId().hashCode());

        if (decision == FraudDecision.BLOCK) {
            FraudBlockedEvent event = FraudBlockedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTime(evalTime)
                    .eventType("FRAUD_BLOCKED")
                    .correlationId(correlationId)
                    .payerUpiId(request.getPayerUpiId())
                    .payeeUpiId(request.getPayeeUpiId())
                    .amount(request.getAmount())
                    .riskScore(finalRiskScore)
                    .triggeredRules(triggeredRules)
                    .reason("Transaction blocked due to high fraud risk score: " + finalRiskScore)
                    .build();

            outboxService.saveOutboxEvent(event.getEventId(), "FRAUD", hashId, "FRAUD_BLOCKED", correlationId, event);
            eventPublisher.publishFraudBlocked(event);
            businessMetricsService.recordTransactionFailure("FRAUD_BLOCKED", "High Risk Score");
        } else if (decision == FraudDecision.REVIEW) {
            HighRiskTransactionEvent event = HighRiskTransactionEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTime(evalTime)
                    .eventType("HIGH_RISK_TRANSACTION")
                    .correlationId(correlationId)
                    .payerUpiId(request.getPayerUpiId())
                    .payeeUpiId(request.getPayeeUpiId())
                    .amount(request.getAmount())
                    .riskScore(finalRiskScore)
                    .triggeredRules(triggeredRules)
                    .build();

            outboxService.saveOutboxEvent(event.getEventId(), "FRAUD", hashId, "HIGH_RISK_TRANSACTION", correlationId, event);
            eventPublisher.publishHighRiskTransaction(event);
        }

        if (!triggeredRules.isEmpty()) {
            FraudDetectedEvent detectedEvent = FraudDetectedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTime(evalTime)
                    .eventType("FRAUD_DETECTED")
                    .correlationId(correlationId)
                    .payerUpiId(request.getPayerUpiId())
                    .payeeUpiId(request.getPayeeUpiId())
                    .amount(request.getAmount())
                    .riskScore(finalRiskScore)
                    .decision(decision.name())
                    .triggeredRules(triggeredRules)
                    .build();

            outboxService.saveOutboxEvent(detectedEvent.getEventId(), "FRAUD", hashId, "FRAUD_DETECTED", correlationId, detectedEvent);
            eventPublisher.publishFraudDetected(detectedEvent);
        }

        log.info("Risk evaluation complete | score: [{}] | decision: [{}] | triggeredRules: [{}]",
                finalRiskScore, decision, triggeredRules);

        return result;
    }

    @Override
    @Transactional
    public FraudRuleResponse createRule(CreateFraudRuleRequest request) {
        log.info("Creating new fraud rule [{}] of type [{}]", request.getRuleName(), request.getType());

        FraudRule rule = new FraudRule();
        rule.setRuleName(request.getRuleName());
        rule.setDescription(request.getDescription());
        rule.setType(request.getType());
        rule.setPriority(request.getPriority() != null ? request.getPriority() : 1);
        rule.setThresholdAmount(request.getThresholdAmount());
        rule.setThresholdCount(request.getThresholdCount());
        rule.setTimeWindowMinutes(request.getTimeWindowMinutes());
        rule.setEnabled(true);

        FraudRule saved = fraudRuleRepository.save(rule);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public FraudRuleResponse updateRule(UUID id, UpdateFraudRuleRequest request) {
        FraudRule rule = fraudRuleRepository.findById(id)
                .orElseThrow(() -> new FraudRuleNotFoundException("Fraud rule not found with ID: " + id));

        if (request.getDescription() != null) {
            rule.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }
        if (request.getPriority() != null) {
            rule.setPriority(request.getPriority());
        }
        if (request.getThresholdAmount() != null) {
            rule.setThresholdAmount(request.getThresholdAmount());
        }
        if (request.getThresholdCount() != null) {
            rule.setThresholdCount(request.getThresholdCount());
        }
        if (request.getTimeWindowMinutes() != null) {
            rule.setTimeWindowMinutes(request.getTimeWindowMinutes());
        }

        FraudRule updated = fraudRuleRepository.save(rule);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRule(UUID id) {
        FraudRule rule = fraudRuleRepository.findById(id)
                .orElseThrow(() -> new FraudRuleNotFoundException("Fraud rule not found with ID: " + id));
        fraudRuleRepository.delete(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudRuleResponse> getAllRules() {
        return fraudRuleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FraudLogResponse> getFraudHistory() {
        return new ArrayList<>(fraudAuditLogs);
    }

    @Override
    public List<FraudLogResponse> getHighRiskTransactions() {
        return fraudAuditLogs.stream()
                .filter(l -> l.getDecision() == FraudDecision.BLOCK || l.getDecision() == FraudDecision.REVIEW)
                .collect(Collectors.toList());
    }

    @Override
    public void recordPinFailure(String upiId) {
        String key = PIN_FAIL_PREFIX + upiId;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(30));
        log.warn("Recorded PIN failure for UPI ID [{}] | count: [{}]", upiId, count);
    }

    @Override
    public void resetPinFailures(String upiId) {
        redisTemplate.delete(PIN_FAIL_PREFIX + upiId);
    }

    @Override
    public void blacklistUpi(String upiId) {
        redisTemplate.opsForSet().add(BLACKLIST_UPI_KEY, upiId);
        log.warn("UPI ID [{}] added to Fraud Blacklist", upiId);
    }

    @Override
    public void blacklistDevice(String deviceId) {
        redisTemplate.opsForSet().add(BLACKLIST_DEVICE_KEY, deviceId);
        log.warn("Device [{}] added to Fraud Blacklist", deviceId);
    }

    private Long incrementRedisCounter(String key, int windowMinutes) {
        try {
            Long val = redisTemplate.opsForValue().increment(key);
            if (val != null && val == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(windowMinutes));
            }
            return val;
        } catch (Exception e) {
            log.error("Redis velocity counter error for key [{}]", key, e);
            return 1L;
        }
    }

    private FraudRuleResponse mapToResponse(FraudRule rule) {
        return new FraudRuleResponse(
                rule.getId(),
                rule.getRuleName(),
                rule.getDescription(),
                rule.getEnabled(),
                rule.getPriority(),
                rule.getType(),
                rule.getThresholdAmount(),
                rule.getThresholdCount(),
                rule.getTimeWindowMinutes(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
