package com.example.demo.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link BusinessMetricsService} using Micrometer {@link MeterRegistry}.
 * <p>
 * Standardizes metric instrumentation across counters, timers, and distribution summaries
 * with structured tags, ensuring zero pollution of core domain logic.
 * </p>
 *
 * @author Fintech UPI Simulator Architecture Team
 * @version 1.0
 * @since Phase 8 - Step 2
 */
@Service
public class BusinessMetricsServiceImpl implements BusinessMetricsService {

    private final MeterRegistry meterRegistry;

    /**
     * Constructs a new {@link BusinessMetricsServiceImpl} with the provided {@link MeterRegistry}.
     *
     * @param meterRegistry the Spring Boot Micrometer meter registry
     */
    public BusinessMetricsServiceImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordTransactionSuccess(String paymentType) {
        Counter.builder(MetricNames.TRANSACTION_SUCCESS)
                .tag("status", "SUCCESS")
                .tag("paymentType", paymentType != null ? paymentType : "UPI_PAYMENT")
                .description("Number of successful transactions executed")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordTransactionFailure(String paymentType, String failureReason) {
        Counter.builder(MetricNames.TRANSACTION_FAILURE)
                .tag("status", "FAILURE")
                .tag("paymentType", paymentType != null ? paymentType : "UPI_PAYMENT")
                .tag("failureReason", failureReason != null ? failureReason : "UNKNOWN")
                .description("Number of failed transactions executed")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordTransactionAmount(double amount, String paymentType) {
        DistributionSummary.builder(MetricNames.TRANSACTION_TOTAL_AMOUNT)
                .tag("paymentType", paymentType != null ? paymentType : "UPI_PAYMENT")
                .baseUnit("INR")
                .description("Distribution summary of transaction monetary amounts")
                .register(meterRegistry)
                .record(amount);
    }

    @Override
    public void recordTransactionProcessingTime(long durationMillis, String paymentType) {
        Timer.builder(MetricNames.TRANSACTION_PROCESSING_TIME)
                .tag("paymentType", paymentType != null ? paymentType : "UPI_PAYMENT")
                .description("Timer tracking transaction processing duration")
                .register(meterRegistry)
                .record(durationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordPaymentRequestCreated() {
        Counter.builder(MetricNames.PAYMENT_REQUEST_CREATED)
                .tag("status", "CREATED")
                .description("Number of payment collect requests created")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordPaymentRequestAccepted() {
        Counter.builder(MetricNames.PAYMENT_REQUEST_ACCEPTED)
                .tag("status", "ACCEPTED")
                .description("Number of payment collect requests accepted")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordPaymentRequestRejected() {
        Counter.builder(MetricNames.PAYMENT_REQUEST_REJECTED)
                .tag("status", "REJECTED")
                .description("Number of payment collect requests rejected")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordPaymentRequestCancelled() {
        Counter.builder(MetricNames.PAYMENT_REQUEST_CANCELLED)
                .tag("status", "CANCELLED")
                .description("Number of payment collect requests cancelled")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordPaymentRequestExpired() {
        Counter.builder(MetricNames.PAYMENT_REQUEST_EXPIRED)
                .tag("status", "EXPIRED")
                .description("Number of payment collect requests expired")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordKafkaEventSent(String topic) {
        Counter.builder(MetricNames.KAFKA_EVENT_SENT)
                .tag("topic", topic != null ? topic : "unknown")
                .tag("result", "SUCCESS")
                .description("Number of events successfully published to Kafka")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordKafkaEventFailed(String topic, String reason) {
        Counter.builder(MetricNames.KAFKA_EVENT_FAILED)
                .tag("topic", topic != null ? topic : "unknown")
                .tag("result", "FAILURE")
                .tag("reason", reason != null ? reason : "UNKNOWN")
                .description("Number of events failed to publish to Kafka")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordOutboxCreated(String eventType) {
        Counter.builder(MetricNames.OUTBOX_CREATED)
                .tag("eventType", eventType != null ? eventType : "unknown")
                .description("Number of outbox events written to database")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordOutboxPublished(String eventType) {
        Counter.builder(MetricNames.OUTBOX_PUBLISHED)
                .tag("eventType", eventType != null ? eventType : "unknown")
                .description("Number of outbox events successfully dispatched to message bus")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordOutboxFailed(String eventType, String reason) {
        Counter.builder(MetricNames.OUTBOX_FAILED)
                .tag("eventType", eventType != null ? eventType : "unknown")
                .tag("reason", reason != null ? reason : "UNKNOWN")
                .description("Number of outbox events failed during dispatching")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRedisCacheHit(String cacheName) {
        Counter.builder(MetricNames.REDIS_CACHE_HIT)
                .tag("cache", cacheName != null ? cacheName : "default")
                .tag("result", "HIT")
                .description("Number of Redis cache hits")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRedisCacheMiss(String cacheName) {
        Counter.builder(MetricNames.REDIS_CACHE_MISS)
                .tag("cache", cacheName != null ? cacheName : "default")
                .tag("result", "MISS")
                .description("Number of Redis cache misses")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRedisLockAcquired(String lockKey) {
        Counter.builder(MetricNames.REDIS_LOCK_ACQUIRED)
                .tag("lock", lockKey != null ? lockKey : "global")
                .tag("result", "ACQUIRED")
                .description("Number of distributed locks successfully acquired")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRedisLockFailed(String lockKey) {
        Counter.builder(MetricNames.REDIS_LOCK_FAILED)
                .tag("lock", lockKey != null ? lockKey : "global")
                .tag("result", "FAILED")
                .description("Number of distributed lock acquisitions failed")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordOtpGenerated() {
        Counter.builder(MetricNames.OTP_GENERATED)
                .tag("type", "SMS_OTP")
                .description("Number of OTP codes generated")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordOtpVerified() {
        Counter.builder(MetricNames.OTP_VERIFIED)
                .tag("result", "SUCCESS")
                .description("Number of OTP codes successfully verified")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordOtpFailed() {
        Counter.builder(MetricNames.OTP_FAILED)
                .tag("result", "FAILURE")
                .description("Number of OTP code verification failures")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordAuthenticationSuccess() {
        Counter.builder(MetricNames.AUTHENTICATION_SUCCESS)
                .tag("result", "SUCCESS")
                .description("Number of successful authentications")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordAuthenticationFailure(String reason) {
        Counter.builder(MetricNames.AUTHENTICATION_FAILURE)
                .tag("result", "FAILURE")
                .tag("reason", reason != null ? reason : "INVALID_CREDENTIALS")
                .description("Number of authentication failures")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordJwtInvalid() {
        Counter.builder(MetricNames.JWT_INVALID)
                .tag("result", "INVALID_JWT")
                .description("Number of invalid or expired JWT token access attempts")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRateLimitAllowed(String key) {
        Counter.builder(MetricNames.RATE_LIMIT_ALLOWED)
                .tag("key", key != null ? key : "global")
                .tag("result", "ALLOWED")
                .description("Number of requests allowed by rate limiter")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRateLimitBlocked(String key) {
        Counter.builder(MetricNames.RATE_LIMIT_BLOCKED)
                .tag("key", key != null ? key : "global")
                .tag("result", "BLOCKED")
                .description("Number of requests blocked by rate limiter")
                .register(meterRegistry)
                .increment();
    }
}
