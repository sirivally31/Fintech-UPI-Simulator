package com.example.demo.entity;

/**
 * Enum representing the type of Merchant QR Code.
 * STATIC: Permanent QR code for merchant, amount and details can be specified at scanning.
 * DYNAMIC: Single-use or timed QR code generated for a specific transaction amount.
 */
public enum QRType {
    STATIC,
    DYNAMIC
}
