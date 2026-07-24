package com.example.demo.service;

import com.example.demo.entity.OutboxEvent;

import java.util.UUID;

/**
 * Service contract for persisting domain events into the Outbox table
 * within local database transactions.
 */
public interface OutboxService {

    OutboxEvent saveOutboxEvent(UUID eventId, String aggregateType, Long aggregateId, String eventType, String correlationId, Object eventPayload);
}
