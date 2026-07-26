package com.example.demo.metrics;

/**
 * Enterprise Metric Constants for the Fintech UPI Payment Simulator.
 * <p>
 * Standardized metric naming conventions matching production standards of PhonePe,
 * Razorpay, Stripe, and Google Pay.
 * </p>
 *
 * @author Fintech UPI Simulator Architecture Team
 * @version 1.0
 * @since Phase 8 - Step 2
 */
public final class MetricNames {

    private MetricNames() {
        // Private constructor to prevent instantiation
    }

    // Transaction Metrics
    public static final String TRANSACTION_SUCCESS = "transaction.success";
    public static final String TRANSACTION_FAILURE = "transaction.failure";
    public static final String TRANSACTION_TOTAL_AMOUNT = "transaction.total.amount";
    public static final String TRANSACTION_PROCESSING_TIME = "transaction.processing.time";

    // Payment Request Metrics
    public static final String PAYMENT_REQUEST_CREATED = "paymentrequest.created";
    public static final String PAYMENT_REQUEST_ACCEPTED = "paymentrequest.accepted";
    public static final String PAYMENT_REQUEST_REJECTED = "paymentrequest.rejected";
    public static final String PAYMENT_REQUEST_CANCELLED = "paymentrequest.cancelled";
    public static final String PAYMENT_REQUEST_EXPIRED = "paymentrequest.expired";

    // Kafka Metrics
    public static final String KAFKA_EVENT_SENT = "kafka.event.sent";
    public static final String KAFKA_EVENT_FAILED = "kafka.event.failed";

    // Outbox Metrics
    public static final String OUTBOX_CREATED = "outbox.created";
    public static final String OUTBOX_PUBLISHED = "outbox.published";
    public static final String OUTBOX_FAILED = "outbox.failed";

    // Redis Metrics
    public static final String REDIS_CACHE_HIT = "redis.cache.hit";
    public static final String REDIS_CACHE_MISS = "redis.cache.miss";
    public static final String REDIS_LOCK_ACQUIRED = "redis.lock.acquired";
    public static final String REDIS_LOCK_FAILED = "redis.lock.failed";

    // OTP Metrics
    public static final String OTP_GENERATED = "otp.generated";
    public static final String OTP_VERIFIED = "otp.verified";
    public static final String OTP_FAILED = "otp.failed";

    // Security Metrics
    public static final String AUTHENTICATION_SUCCESS = "authentication.success";
    public static final String AUTHENTICATION_FAILURE = "authentication.failure";
    public static final String JWT_INVALID = "jwt.invalid";

    // Rate Limiter Metrics
    public static final String RATE_LIMIT_ALLOWED = "ratelimit.allowed";
    public static final String RATE_LIMIT_BLOCKED = "ratelimit.blocked";
}
