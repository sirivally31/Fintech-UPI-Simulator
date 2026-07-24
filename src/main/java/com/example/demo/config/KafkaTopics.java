package com.example.demo.config;

/**
 * Constants class for Kafka topic names in the UPI simulator.
 * Centralizing topic names prevents magic strings across producers and consumers.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Private constructor to prevent instantiation of utility class
    }

    public static final String TRANSACTION_COMPLETED = "upi.transaction.completed";
    public static final String PAYMENT_REQUEST_CREATED = "upi.payment-request.created";
    public static final String PAYMENT_REQUEST_ACCEPTED = "upi.payment-request.accepted";
    public static final String PAYMENT_REQUEST_REJECTED = "upi.payment-request.rejected";
    public static final String PAYMENT_REQUEST_CANCELLED = "upi.payment-request.cancelled";
}
