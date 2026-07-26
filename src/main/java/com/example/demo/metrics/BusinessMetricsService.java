package com.example.demo.metrics;

import java.util.concurrent.TimeUnit;

/**
 * Interface defining enterprise business KPI metric collection methods.
 * <p>
 * Decouples core payment, messaging, security, and caching domain logic from
 * the underlying Micrometer metrics framework.
 * </p>
 *
 * @author Fintech UPI Simulator Architecture Team
 * @version 1.0
 * @since Phase 8 - Step 2
 */
public interface BusinessMetricsService {

    // Transaction Metrics
    void recordTransactionSuccess(String paymentType);
    void recordTransactionFailure(String paymentType, String failureReason);
    void recordTransactionAmount(double amount, String paymentType);
    void recordTransactionProcessingTime(long durationMillis, String paymentType);

    // Payment Request Metrics
    void recordPaymentRequestCreated();
    void recordPaymentRequestAccepted();
    void recordPaymentRequestRejected();
    void recordPaymentRequestCancelled();
    void recordPaymentRequestExpired();

    // Kafka Metrics
    void recordKafkaEventSent(String topic);
    void recordKafkaEventFailed(String topic, String reason);

    // Outbox Metrics
    void recordOutboxCreated(String eventType);
    void recordOutboxPublished(String eventType);
    void recordOutboxFailed(String eventType, String reason);

    // Redis Metrics
    void recordRedisCacheHit(String cacheName);
    void recordRedisCacheMiss(String cacheName);
    void recordRedisLockAcquired(String lockKey);
    void recordRedisLockFailed(String lockKey);

    // OTP Metrics
    void recordOtpGenerated();
    void recordOtpVerified();
    void recordOtpFailed();

    // Security Metrics
    void recordAuthenticationSuccess();
    void recordAuthenticationFailure(String reason);
    void recordJwtInvalid();

    // Rate Limiter Metrics
    void recordRateLimitAllowed(String key);
    void recordRateLimitBlocked(String key);
}
