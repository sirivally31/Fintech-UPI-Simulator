package com.example.demo.entity;

/**
 * Enum representing execution lifecycle status for settlement batches and entries.
 */
public enum SettlementStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVERSED
}
