package com.example.demo.entity;

/**
 * Enumeration representing the processing lifecycle status of an OutboxEvent.
 */
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
