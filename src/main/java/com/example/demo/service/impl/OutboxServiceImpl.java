package com.example.demo.service.impl;

import com.example.demo.entity.OutboxEvent;
import com.example.demo.repository.OutboxEventRepository;
import com.example.demo.service.OutboxEventMapper;
import com.example.demo.service.OutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.example.demo.metrics.BusinessMetricsService;

/**
 * Service implementation for serializing payloads and persisting OutboxEvent entities
 * within local database transactions.
 */
@Service
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventMapper outboxEventMapper;
    private final BusinessMetricsService businessMetricsService;

    public OutboxServiceImpl(OutboxEventRepository outboxEventRepository,
                             OutboxEventMapper outboxEventMapper,
                             BusinessMetricsService businessMetricsService) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxEventMapper = outboxEventMapper;
        this.businessMetricsService = businessMetricsService;
    }

    @Override
    @Transactional
    public OutboxEvent saveOutboxEvent(UUID eventId, String aggregateType, Long aggregateId, String eventType, String correlationId, Object eventPayload) {
        String jsonPayload = outboxEventMapper.serialize(eventPayload);
        OutboxEvent outboxEvent = new OutboxEvent(eventId, aggregateType, aggregateId, eventType, jsonPayload, correlationId);
        OutboxEvent saved = outboxEventRepository.save(outboxEvent);
        businessMetricsService.recordOutboxCreated(eventType);
        return saved;
    }
}
