package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Settlement Batch container aggregating transaction entries for clearing.
 */
@Entity
@Table(name = "settlement_batches")
public class SettlementBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String batchReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(nullable = false)
    private Integer totalRecords = 0;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDebitAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCreditAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netSettlementAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;

    public SettlementBatch() {
    }

    public SettlementBatch(UUID id, String batchReference, SettlementType type, SettlementStatus status, 
                           Integer totalRecords, BigDecimal totalDebitAmount, BigDecimal totalCreditAmount, 
                           BigDecimal netSettlementAmount, Integer retryCount, LocalDateTime createdAt, 
                           LocalDateTime processedAt, LocalDateTime completedAt) {
        this.id = id;
        this.batchReference = batchReference;
        this.type = type;
        this.status = status;
        this.totalRecords = totalRecords;
        this.totalDebitAmount = totalDebitAmount;
        this.totalCreditAmount = totalCreditAmount;
        this.netSettlementAmount = netSettlementAmount;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
        this.completedAt = completedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.status == null) {
            this.status = SettlementStatus.PENDING;
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBatchReference() {
        return batchReference;
    }

    public void setBatchReference(String batchReference) {
        this.batchReference = batchReference;
    }

    public SettlementType getType() {
        return type;
    }

    public void setType(SettlementType type) {
        this.type = type;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public BigDecimal getTotalDebitAmount() {
        return totalDebitAmount;
    }

    public void setTotalDebitAmount(BigDecimal totalDebitAmount) {
        this.totalDebitAmount = totalDebitAmount;
    }

    public BigDecimal getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public void setTotalCreditAmount(BigDecimal totalCreditAmount) {
        this.totalCreditAmount = totalCreditAmount;
    }

    public BigDecimal getNetSettlementAmount() {
        return netSettlementAmount;
    }

    public void setNetSettlementAmount(BigDecimal netSettlementAmount) {
        this.netSettlementAmount = netSettlementAmount;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
