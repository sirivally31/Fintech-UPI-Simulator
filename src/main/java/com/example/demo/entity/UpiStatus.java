package com.example.demo.entity;

/**
 * Enum representing the status of a UPI ID.
 * 
 * ACTIVE: The UPI ID is active and can be used for transactions.
 * INACTIVE: The UPI ID is temporarily disabled.
 * BLOCKED: The UPI ID is blocked due to security reasons or violations.
 */
public enum UpiStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED
}
