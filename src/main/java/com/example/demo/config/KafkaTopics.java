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
    public static final String QR_CREATED = "upi.qr.created";
    public static final String QR_PAYMENT_SUCCESS = "upi.qr.payment.success";
    public static final String BENEFICIARY_ADDED = "upi.beneficiary.added";
    public static final String BENEFICIARY_UPDATED = "upi.beneficiary.updated";
    public static final String BENEFICIARY_DELETED = "upi.beneficiary.deleted";
    public static final String AUTOPAY_CREATED = "upi.autopay.created";
    public static final String AUTOPAY_EXECUTED = "upi.autopay.executed";
    public static final String AUTOPAY_FAILED = "upi.autopay.failed";
    public static final String AUTOPAY_CANCELLED = "upi.autopay.cancelled";
    public static final String FRAUD_DETECTED = "upi.fraud.detected";
    public static final String FRAUD_BLOCKED = "upi.fraud.blocked";
    public static final String HIGH_RISK_TRANSACTION = "upi.fraud.high-risk";
    public static final String SETTLEMENT_COMPLETED = "upi.settlement.completed";
    public static final String SETTLEMENT_FAILED = "upi.settlement.failed";
    public static final String SETTLEMENT_RECONCILED = "upi.settlement.reconciled";
    public static final String SETTLEMENT_REVERSED = "upi.settlement.reversed";
    public static final String AUDIT_CREATED = "upi.audit.created";
}
