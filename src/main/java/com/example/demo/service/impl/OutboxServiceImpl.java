package com.example.demo.service.impl;

import com.example.demo.entity.OutboxEvent;
import com.example.demo.repository.OutboxEventRepository;
import com.example.demo.service.OutboxEventMapper;
import com.example.demo.service.OutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for serializing payloads and persisting OutboxEvent entities
 * within local database transactions.
 */
@Service
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventMapper outboxEventMapper;

    public OutboxServiceImpl(OutboxEventRepository outboxEventRepository,
                             OutboxEventMapper outboxEventMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxEventMapper = outboxEventMapper;
    }

    @Override
    @Transactional
    public OutboxEvent saveOutboxEvent(UUID eventId, String aggregateType, Long aggregateId, String eventType, String correlationId, Object eventPayload) {
        String jsonPayload = outboxEventMapper.serialize(eventPayload);
        OutboxEvent outboxEvent = new OutboxEvent(eventId, aggregateType, aggregateId, eventType, jsonPayload, correlationId);
        return outboxEventRepository.save(outboxEvent);
    }
}
