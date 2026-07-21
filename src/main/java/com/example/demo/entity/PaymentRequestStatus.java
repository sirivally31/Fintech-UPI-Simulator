package com.example.demo.entity;

/**
 * Enum representing the lifecycle status of a Payment Request (Collect Request).
 * 
 * WHY WE USE ENUMS FOR STATUS:
 * Using an Enum ensures type safety and prevents arbitrary strings from being inserted 
 * into the database. It strictly limits the possible states a payment request can be in, 
 * which is critical for the finite state machine of a financial transaction.
 */
public enum PaymentRequestStatus {
    /**
     * Request has been created and sent to the sender, waiting for their response.
     */
    PENDING,

    /**
     * Sender has authorized the request with their UPI PIN, and money has been transferred.
     */
    ACCEPTED,

    /**
     * Sender explicitly declined the request.
     */
    REJECTED,

    /**
     * Request surpassed its 24-hour validity window without being accepted or rejected.
     */
    EXPIRED,

    /**
     * The requester (receiver) cancelled the request before the sender could respond.
     */
    CANCELLED
}
