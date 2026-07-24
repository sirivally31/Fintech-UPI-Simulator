package com.example.demo.service;

/**
 * Service contract for background polling and publishing of pending OutboxEvent records to Kafka.
 */
public interface OutboxPublisherService {

    void processPendingOutboxEvents();
}
