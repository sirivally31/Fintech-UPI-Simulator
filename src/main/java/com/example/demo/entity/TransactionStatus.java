package com.example.demo.entity;

/**
 * Represents the various states a financial transaction can be in.
 * 
 * Why payment systems use transaction statuses instead of deleting failed transactions:
 * In a financial system, an audit trail is critical. If a transaction fails, we never delete the record 
 * from the database. Instead, we mark it as FAILED. This allows customer support to investigate issues, 
 * helps in dispute resolution, provides metrics on failure rates, and ensures full compliance with 
 * financial regulations. Every attempt, whether successful or not, must be permanently recorded.
 */
public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REVERSED
}
