package com.example.demo.entity;

/**
 * Enum defining business and security actions for enterprise audit logging.
 */
public enum AuditAction {
    LOGIN,
    LOGIN_FAILED,
    REGISTER,
    TRANSFER,
    QR_PAYMENT,
    CREATE,
    UPDATE,
    DELETE,
    APPROVE,
    REJECT,
    SETTLEMENT,
    FRAUD_BLOCK,
    ADMIN_ACTION,
    NOTIFICATION,
    SYSTEM
}
