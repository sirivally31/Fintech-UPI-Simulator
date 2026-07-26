package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ledger reconciliation audit verification summary")
public class ReconciliationSummaryResponse {

    @Schema(description = "Total ledger debits verified", example = "1500000.00")
    private BigDecimal totalDebitAmount;

    @Schema(description = "Total ledger credits verified", example = "1500000.00")
    private BigDecimal totalCreditAmount;

    @Schema(description = "Net balance difference", example = "0.00")
    private BigDecimal netBalanceDifference;

    @Schema(description = "Total merchant pool balance", example = "450000.00")
    private BigDecimal totalMerchantBalance;

    @Schema(description = "Pending settlements count", example = "0")
    private long pendingSettlementCount;

    @Schema(description = "Failed settlements count", example = "0")
    private long failedSettlementCount;

    @Schema(description = "Duplicate transactions detected count", example = "0")
    private long duplicateTransactionCount;

    @Schema(description = "Missing ledger entries count", example = "0")
    private long missingEntryCount;

    @Schema(description = "Reconciliation status (RECONCILED, DISCREPANCY_FOUND)", example = "RECONCILED")
    private String reconciliationStatus;

    @Schema(description = "Reconciliation execution timestamp")
    private LocalDateTime timestamp;

    public ReconciliationSummaryResponse() {
    }

    public ReconciliationSummaryResponse(BigDecimal totalDebitAmount, BigDecimal totalCreditAmount, 
                                         BigDecimal netBalanceDifference, BigDecimal totalMerchantBalance, 
                                         long pendingSettlementCount, long failedSettlementCount, 
                                         long duplicateTransactionCount, long missingEntryCount, 
                                         String reconciliationStatus, LocalDateTime timestamp) {
        this.totalDebitAmount = totalDebitAmount;
        this.totalCreditAmount = totalCreditAmount;
        this.netBalanceDifference = netBalanceDifference;
        this.totalMerchantBalance = totalMerchantBalance;
        this.pendingSettlementCount = pendingSettlementCount;
        this.failedSettlementCount = failedSettlementCount;
        this.duplicateTransactionCount = duplicateTransactionCount;
        this.missingEntryCount = missingEntryCount;
        this.reconciliationStatus = reconciliationStatus;
        this.timestamp = timestamp;
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

    public BigDecimal getNetBalanceDifference() {
        return netBalanceDifference;
    }

    public void setNetBalanceDifference(BigDecimal netBalanceDifference) {
        this.netBalanceDifference = netBalanceDifference;
    }

    public BigDecimal getTotalMerchantBalance() {
        return totalMerchantBalance;
    }

    public void setTotalMerchantBalance(BigDecimal totalMerchantBalance) {
        this.totalMerchantBalance = totalMerchantBalance;
    }

    public long getPendingSettlementCount() {
        return pendingSettlementCount;
    }

    public void setPendingSettlementCount(long pendingSettlementCount) {
        this.pendingSettlementCount = pendingSettlementCount;
    }

    public long getFailedSettlementCount() {
        return failedSettlementCount;
    }

    public void setFailedSettlementCount(long failedSettlementCount) {
        this.failedSettlementCount = failedSettlementCount;
    }

    public long getDuplicateTransactionCount() {
        return duplicateTransactionCount;
    }

    public void setDuplicateTransactionCount(long duplicateTransactionCount) {
        this.duplicateTransactionCount = duplicateTransactionCount;
    }

    public long getMissingEntryCount() {
        return missingEntryCount;
    }

    public void setMissingEntryCount(long missingEntryCount) {
        this.missingEntryCount = missingEntryCount;
    }

    public String getReconciliationStatus() {
        return reconciliationStatus;
    }

    public void setReconciliationStatus(String reconciliationStatus) {
        this.reconciliationStatus = reconciliationStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
