package com.example.demo.config;

import org.springframework.stereotype.Component;

/**
 * Utility component mapping eventType strings to corresponding Kafka topic constants.
 * Prevents magic topic strings across outbox processors and publishers.
 */
@Component
public class TopicResolver {

    public String resolveTopic(String eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("eventType cannot be null");
        }

        return switch (eventType.toUpperCase()) {
            case "TRANSACTION_COMPLETED", "TRANSACTIONCOMPLETED" -> KafkaTopics.TRANSACTION_COMPLETED;
            case "PAYMENT_REQUEST_CREATED", "PAYMENTREQUESTCREATED" -> KafkaTopics.PAYMENT_REQUEST_CREATED;
            case "PAYMENT_REQUEST_ACCEPTED", "PAYMENTREQUESTACCEPTED" -> KafkaTopics.PAYMENT_REQUEST_ACCEPTED;
            case "PAYMENT_REQUEST_REJECTED", "PAYMENTREQUESTREJECTED" -> KafkaTopics.PAYMENT_REQUEST_REJECTED;
            case "PAYMENT_REQUEST_CANCELLED", "PAYMENTREQUESTCANCELLED" -> KafkaTopics.PAYMENT_REQUEST_CANCELLED;
            default -> throw new IllegalArgumentException("Unknown eventType for topic resolution: " + eventType);
        };
    }
}
