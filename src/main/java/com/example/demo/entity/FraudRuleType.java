package com.example.demo.entity;

/**
 * Enum representing supported fraud rule detection types.
 */
public enum FraudRuleType {
    HIGH_VALUE_TRANSACTION,
    RAPID_TRANSACTIONS,
    VELOCITY_LIMIT,
    BLACKLISTED_UPI,
    BLACKLISTED_DEVICE,
    SUSPICIOUS_LOCATION,
    MULTIPLE_FAILED_PIN,
    NEW_DEVICE_TRANSACTION
}
