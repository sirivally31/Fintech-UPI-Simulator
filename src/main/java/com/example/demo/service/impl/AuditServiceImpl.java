package com.example.demo.service.impl;

import com.example.demo.dto.AuditLogResponse;
import com.example.demo.dto.AuditSearchRequest;
import com.example.demo.dto.AuditSummaryResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.events.AuditCreatedEvent;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.service.AuditService;
import com.example.demo.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for enterprise audit logging, searching, caching, and outbox event dispatching.
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    private final OutboxService outboxService;

    public AuditServiceImpl(AuditLogRepository auditLogRepository, OutboxService outboxService) {
        this.auditLogRepository = auditLogRepository;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"auditSummary", "recentAudits"}, allEntries = true)
    public AuditLogResponse log(AuditLog auditLog) {
        if (auditLog == null) {
            throw new IllegalArgumentException("AuditLog entity cannot be null");
        }

        AuditLog savedLog = auditLogRepository.save(auditLog);
        log.info("Persisted audit log entry ID [{}] | Action: [{}] | Module: [{}] | User: [{}]",
                savedLog.getId(), savedLog.getAction(), savedLog.getModule(), savedLog.getUsername());

        // Publish event through Outbox pattern
        AuditCreatedEvent event = AuditCreatedEvent.fromEntity(savedLog);
        if (event != null && event.getEventId() != null) {
            try {
                UUID eventUuid = UUID.fromString(savedLog.getEventId());
                outboxService.saveOutboxEvent(
                        eventUuid,
                        "AUDIT_LOG",
                        savedLog.getId(),
                        "AUDIT_CREATED",
                        savedLog.getEventId(),
                        event
                );
            } catch (Exception e) {
                log.warn("Failed to create outbox event for audit log [{}]", savedLog.getId(), e);
            }
        }

        return AuditLogResponse.fromEntity(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(AuditSearchRequest request) {
        if (request == null) {
            request = new AuditSearchRequest();
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, request.getSortBy()));

        final AuditSearchRequest finalRequest = request;

        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (finalRequest.getUsername() != null && !finalRequest.getUsername().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("username")), finalRequest.getUsername().toLowerCase()));
            }
            if (finalRequest.getModule() != null && !finalRequest.getModule().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("module")), finalRequest.getModule().toUpperCase()));
            }
            if (finalRequest.getAction() != null) {
                predicates.add(cb.equal(root.get("action"), finalRequest.getAction()));
            }
            if (finalRequest.getSuccess() != null) {
                predicates.add(cb.equal(root.get("success"), finalRequest.getSuccess()));
            }
            if (finalRequest.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), finalRequest.getStartDate()));
            }
            if (finalRequest.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), finalRequest.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> auditLogs = auditLogRepository.findAll(spec, pageable);
        return auditLogs.map(AuditLogResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with id: " + id));
        return AuditLogResponse.fromEntity(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "auditSummary", key = "'summary'")
    public AuditSummaryResponse getSummary() {
        long totalLogs = auditLogRepository.count();
        long successLogs = auditLogRepository.countBySuccess(true);
        long failedLogs = auditLogRepository.countBySuccess(false);

        List<Object[]> moduleGroupResults = auditLogRepository.countLogEntriesByModule();
        Map<String, Long> moduleCounts = new HashMap<>();
        for (Object[] row : moduleGroupResults) {
            String moduleName = (String) row[0];
            Long count = (Long) row[1];
            moduleCounts.put(moduleName != null ? moduleName : "UNKNOWN", count);
        }

        return new AuditSummaryResponse(totalLogs, successLogs, failedLogs, moduleCounts);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "recentAudits", key = "'recent'")
    public List<AuditLogResponse> getRecent() {
        List<AuditLog> recentLogs = auditLogRepository.findTop20ByOrderByTimestampDesc();
        return recentLogs.stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] export(AuditSearchRequest request) {
        Page<AuditLogResponse> results = search(request);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,EventID,Timestamp,Username,Action,Module,Success,Method,URI,ClientIP,ExecutionTimeMs\n");

        for (AuditLogResponse item : results.getContent()) {
            csv.append(item.getId()).append(",")
                    .append(item.getEventId()).append(",")
                    .append(item.getTimestamp()).append(",")
                    .append(sanitizeCsv(item.getUsername())).append(",")
                    .append(item.getAction()).append(",")
                    .append(sanitizeCsv(item.getModule())).append(",")
                    .append(item.getSuccess()).append(",")
                    .append(item.getRequestMethod()).append(",")
                    .append(sanitizeCsv(item.getRequestUri())).append(",")
                    .append(sanitizeCsv(item.getClientIp())).append(",")
                    .append(item.getExecutionTime()).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String sanitizeCsv(String input) {
        if (input == null) return "";
        return "\"" + input.replace("\"", "\"\"") + "\"";
    }
}
