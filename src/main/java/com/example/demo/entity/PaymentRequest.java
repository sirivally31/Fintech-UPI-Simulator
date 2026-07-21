package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a Request for Money (Collect Request) in the UPI system.
 * 
 * HOW SPRING DATA JPA WORKS HERE:
 * The @Entity annotation registers this class with the Hibernate context. 
 * Hibernate reads the @Table, @Column, and @ManyToOne annotations at startup to 
 * generate the SQL schema. It translates Java object relationships into foreign keys.
 */
@Entity
@Table(name = "payment_requests")
public class PaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * A unique string identifier (similar to a UTR) to prevent duplicate requests 
     * and allow tracking.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String requestReference;

    /**
     * The UPI ID of the person who is being asked to pay (the Sender of money).
     * We reuse the existing UpiId entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_upi_id", nullable = false)
    private UpiId senderUpiId;

    /**
     * The UPI ID of the person who initiated the request (the Receiver of money).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_upi_id", nullable = false)
    private UpiId receiverUpiId;

    /**
     * BigDecimal is mandatory for all financial amounts to avoid floating point inaccuracies.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Optional message attached to the request (e.g., "For dinner").
     */
    @Column(length = 255)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentRequestStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp of when the request was accepted, rejected, or cancelled.
     */
    @Column
    private LocalDateTime respondedAt;

    public PaymentRequest() {
    }

    /**
     * PrePersist ensures we always have a createdAt timestamp before inserting into DB.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            // Standard UPI collect requests typically expire in 24 hours
            expiresAt = createdAt.plusHours(24);
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(String requestReference) {
        this.requestReference = requestReference;
    }

    public UpiId getSenderUpiId() {
        return senderUpiId;
    }

    public void setSenderUpiId(UpiId senderUpiId) {
        this.senderUpiId = senderUpiId;
    }

    public UpiId getReceiverUpiId() {
        return receiverUpiId;
    }

    public void setReceiverUpiId(UpiId receiverUpiId) {
        this.receiverUpiId = receiverUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PaymentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentRequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
