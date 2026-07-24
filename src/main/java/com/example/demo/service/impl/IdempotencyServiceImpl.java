package com.example.demo.service.impl;

import com.example.demo.entity.ProcessedEvent;
import com.example.demo.repository.ProcessedEventRepository;
import com.example.demo.service.IdempotencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation for managing event idempotency using relational database persistence.
 */
@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyServiceImpl(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEventProcessed(UUID eventId) {
        if (eventId == null) {
            return false;
        }
        return processedEventRepository.existsByEventId(eventId);
    }

    @Override
    @Transactional
    public void markEventProcessed(UUID eventId, String eventType, String correlationId) {
        if (eventId == null) {
            return;
        }
        if (!processedEventRepository.existsByEventId(eventId)) {
            ProcessedEvent processedEvent = new ProcessedEvent(eventId, eventType, correlationId);
            processedEventRepository.save(processedEvent);
        }
    }
}
