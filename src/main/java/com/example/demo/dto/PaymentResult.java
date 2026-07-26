package com.example.demo.dto;

/**
 * Enum indicating detailed outcome status for QR Payment processing.
 */
public enum PaymentResult {
    SUCCESS,
    FAILED_INVALID_PIN,
    FAILED_INSUFFICIENT_FUNDS,
    FAILED_EXPIRED_QR,
    FAILED_USED_QR,
    FAILED_INACTIVE_MERCHANT,
    FAILED_GENERAL
}
