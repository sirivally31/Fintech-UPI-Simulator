package com.example.demo.dto;

import com.example.demo.entity.SettlementStatus;
import com.example.demo.entity.SettlementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response details for a Settlement Batch profile")
public class SettlementBatchResponse {

    @Schema(description = "Batch UUID")
    private UUID id;

    @Schema(description = "Unique batch reference string", example = "SET20260726190000001")
    private String batchReference;

    @Schema(description = "Settlement Type category", example = "UPI_TRANSFER")
    private SettlementType type;

    @Schema(description = "Settlement Status", example = "COMPLETED")
    private SettlementStatus status;

    @Schema(description = "Total transaction records in batch", example = "42")
    private Integer totalRecords;

    @Schema(description = "Total debit amount", example = "125000.00")
    private BigDecimal totalDebitAmount;

    @Schema(description = "Total credit amount", example = "125000.00")
    private BigDecimal totalCreditAmount;

    @Schema(description = "Net settlement amount cleared", example = "125000.00")
    private BigDecimal netSettlementAmount;

    @Schema(description = "Execution retry count", example = "0")
    private Integer retryCount;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Processing timestamp")
    private LocalDateTime processedAt;

    @Schema(description = "Completion timestamp")
    private LocalDateTime completedAt;

    public SettlementBatchResponse() {
    }

    public SettlementBatchResponse(UUID id, String batchReference, SettlementType type, SettlementStatus status, 
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
