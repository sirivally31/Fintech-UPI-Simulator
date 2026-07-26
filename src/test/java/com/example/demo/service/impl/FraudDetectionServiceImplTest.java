package com.example.demo.service.impl;

import com.example.demo.dto.FraudEvaluationRequest;
import com.example.demo.dto.FraudEvaluationResult;
import com.example.demo.entity.FraudDecision;
import com.example.demo.entity.FraudRule;
import com.example.demo.entity.FraudRuleType;
import com.example.demo.metrics.BusinessMetricsService;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.FraudRuleRepository;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceImplTest {

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private BusinessMetricsService businessMetricsService;

    @InjectMocks
    private FraudDetectionServiceImpl fraudDetectionService;

    private FraudRule highValueRule;
    private FraudRule velocityRule;
    private FraudRule blacklistRule;

    @BeforeEach
    void setUp() {
        highValueRule = new FraudRule(UUID.randomUUID(), "High Value Transfer Guard", "Flags high transfers", true, 1,
                FraudRuleType.HIGH_VALUE_TRANSACTION, new BigDecimal("50000.00"), null, null, LocalDateTime.now(), LocalDateTime.now());

        velocityRule = new FraudRule(UUID.randomUUID(), "Rapid Velocity Guard", "Flags >5 transfers in 5m", true, 2,
                FraudRuleType.VELOCITY_LIMIT, null, 5, 5, LocalDateTime.now(), LocalDateTime.now());

        blacklistRule = new FraudRule(UUID.randomUUID(), "Blacklist Guard", "Blocks blacklisted UPIs", true, 3,
                FraudRuleType.BLACKLISTED_UPI, null, null, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @DisplayName("Evaluate Transaction - Normal Low Risk Allowed")
    void testEvaluateTransaction_LowRisk_Allow() {
        FraudEvaluationRequest req = new FraudEvaluationRequest("john@upi", "alice@upi", new BigDecimal("500.00"), "DEV-1", "127.0.0.1");

        when(fraudRuleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(highValueRule, velocityRule));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        FraudEvaluationResult result = fraudDetectionService.evaluateTransaction(req);

        assertNotNull(result);
        assertEquals(0, result.getRiskScore());
        assertEquals(FraudDecision.ALLOW, result.getDecision());
        assertTrue(result.getTriggeredRules().isEmpty());
    }

    @Test
    @DisplayName("Evaluate Transaction - High Value Flagged for Review")
    void testEvaluateTransaction_HighValue_Review() {
        FraudEvaluationRequest req = new FraudEvaluationRequest("john@upi", "alice@upi", new BigDecimal("75000.00"), "DEV-1", "127.0.0.1");

        when(fraudRuleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(highValueRule));

        FraudEvaluationResult result = fraudDetectionService.evaluateTransaction(req);

        assertNotNull(result);
        assertEquals(35, result.getRiskScore());
        assertEquals(FraudDecision.REVIEW, result.getDecision());
        assertEquals(1, result.getTriggeredRules().size());

        verify(outboxService).saveOutboxEvent(any(), eq("FRAUD"), anyLong(), eq("HIGH_RISK_TRANSACTION"), anyString(), any());
        verify(eventPublisher).publishHighRiskTransaction(any());
    }

    @Test
    @DisplayName("Evaluate Transaction - Blacklisted UPI Blocked")
    void testEvaluateTransaction_BlacklistedUPI_Block() {
        FraudEvaluationRequest req = new FraudEvaluationRequest("baduser@upi", "alice@upi", new BigDecimal("100.00"), "DEV-1", "127.0.0.1");

        when(fraudRuleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(blacklistRule));
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.isMember(eq("fraud:blacklist:upi"), eq("baduser@upi"))).thenReturn(true);

        FraudEvaluationResult result = fraudDetectionService.evaluateTransaction(req);

        assertNotNull(result);
        assertEquals(100, result.getRiskScore());
        assertEquals(FraudDecision.BLOCK, result.getDecision());

        verify(outboxService).saveOutboxEvent(any(), eq("FRAUD"), anyLong(), eq("FRAUD_BLOCKED"), anyString(), any());
        verify(eventPublisher).publishFraudBlocked(any());
    }

    @Test
    @DisplayName("Record & Reset PIN Failures - Success")
    void testRecordAndResetPinFailures() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("fraud:pin_failures:john@upi")).thenReturn(1L);

        fraudDetectionService.recordPinFailure("john@upi");
        verify(valueOperations).increment("fraud:pin_failures:john@upi");

        fraudDetectionService.resetPinFailures("john@upi");
        verify(redisTemplate).delete("fraud:pin_failures:john@upi");
    }
}
