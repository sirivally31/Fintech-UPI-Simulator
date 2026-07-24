package com.example.demo.service;

import java.util.UUID;

/**
 * Service contract for checking and recording processed event IDs for consumer idempotency.
 */
public interface IdempotencyService {

    boolean isEventProcessed(UUID eventId);

    void markEventProcessed(UUID eventId, String eventType, String correlationId);
}
