package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response item for AutoPay execution history")
public class AutoPayHistoryResponse {

    @Schema(description = "AutoPay Mandate UUID")
    private UUID autoPayId;

    @Schema(description = "Mandate Reference", example = "MN20260726175000001")
    private String mandateReference;

    @Schema(description = "Transaction Reference", example = "TXN20260726175000001")
    private String transactionReference;

    @Schema(description = "UTR Number", example = "UTR2026072688776655")
    private String utrNumber;

    @Schema(description = "Beneficiary Name", example = "Alice Smith")
    private String beneficiaryName;

    @Schema(description = "Beneficiary UPI ID", example = "alice@upi")
    private String beneficiaryUpiId;

    @Schema(description = "Amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Execution Status", example = "SUCCESS")
    private String status;

    @Schema(description = "Execution Timestamp")
    private LocalDateTime executionTime;

    public AutoPayHistoryResponse() {
    }

    public AutoPayHistoryResponse(UUID autoPayId, String mandateReference, String transactionReference, 
                                  String utrNumber, String beneficiaryName, String beneficiaryUpiId, 
                                  BigDecimal amount, String status, LocalDateTime executionTime) {
        this.autoPayId = autoPayId;
        this.mandateReference = mandateReference;
        this.transactionReference = transactionReference;
        this.utrNumber = utrNumber;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryUpiId = beneficiaryUpiId;
        this.amount = amount;
        this.status = status;
        this.executionTime = executionTime;
    }

    public UUID getAutoPayId() {
        return autoPayId;
    }

    public void setAutoPayId(UUID autoPayId) {
        this.autoPayId = autoPayId;
    }

    public String getMandateReference() {
        return mandateReference;
    }

    public void setMandateReference(String mandateReference) {
        this.mandateReference = mandateReference;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getUtrNumber() {
        return utrNumber;
    }

    public void setUtrNumber(String utrNumber) {
        this.utrNumber = utrNumber;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryUpiId() {
        return beneficiaryUpiId;
    }

    public void setBeneficiaryUpiId(String beneficiaryUpiId) {
        this.beneficiaryUpiId = beneficiaryUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
}
