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
    SYSTEM,
    MERCHANT_APPROVED,
    MERCHANT_REJECTED,
    MERCHANT_SUSPENDED,
    MERCHANT_ACTIVATED,
    USER_LOCKED,
    USER_UNLOCKED,
    USER_ENABLED,
    USER_DISABLED,
    PIN_RESET,
    SYSTEM_CONFIG_UPDATED,
    ROLE_CREATED,
    ROLE_ASSIGNED,
    ROLE_REMOVED,
    DASHBOARD_VIEWED
}
