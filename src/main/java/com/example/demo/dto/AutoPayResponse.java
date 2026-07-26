package com.example.demo.dto;

import com.example.demo.entity.AutoPayFrequency;
import com.example.demo.entity.AutoPayStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response details for an AutoPay mandate profile")
public class AutoPayResponse {

    @Schema(description = "AutoPay mandate UUID", example = "e3b0c442-98fc-11ee-b9d1-0242ac120002")
    private UUID id;

    @Schema(description = "Owner User UPI ID", example = "john@upi")
    private String ownerUpiId;

    @Schema(description = "Beneficiary UUID")
    private UUID beneficiaryId;

    @Schema(description = "Beneficiary Name", example = "Alice Smith")
    private String beneficiaryName;

    @Schema(description = "Beneficiary UPI ID", example = "alice@upi")
    private String beneficiaryUpiId;

    @Schema(description = "Unique mandate reference string", example = "MN20260726175000001")
    private String mandateReference;

    @Schema(description = "Mandate recurring amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Frequency (DAILY, WEEKLY, MONTHLY, YEARLY)", example = "MONTHLY")
    private AutoPayFrequency frequency;

    @Schema(description = "Status (ACTIVE, PAUSED, CANCELLED, FAILED, COMPLETED)", example = "ACTIVE")
    private AutoPayStatus status;

    @Schema(description = "Start Date", example = "2026-08-01")
    private LocalDate startDate;

    @Schema(description = "End Date", example = "2027-08-01")
    private LocalDate endDate;

    @Schema(description = "Scheduled next execution time")
    private LocalDateTime nextExecutionTime;

    @Schema(description = "Execution retry count", example = "0")
    private Integer retryCount;

    @Schema(description = "Is mandate active", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public AutoPayResponse() {
    }

    public AutoPayResponse(UUID id, String ownerUpiId, UUID beneficiaryId, String beneficiaryName, 
                           String beneficiaryUpiId, String mandateReference, BigDecimal amount, 
                           AutoPayFrequency frequency, AutoPayStatus status, LocalDate startDate, 
                           LocalDate endDate, LocalDateTime nextExecutionTime, Integer retryCount, 
                           Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerUpiId = ownerUpiId;
        this.beneficiaryId = beneficiaryId;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryUpiId = beneficiaryUpiId;
        this.mandateReference = mandateReference;
        this.amount = amount;
        this.frequency = frequency;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextExecutionTime = nextExecutionTime;
        this.retryCount = retryCount;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOwnerUpiId() {
        return ownerUpiId;
    }

    public void setOwnerUpiId(String ownerUpiId) {
        this.ownerUpiId = ownerUpiId;
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
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

    public String getMandateReference() {
        return mandateReference;
    }

    public void setMandateReference(String mandateReference) {
        this.mandateReference = mandateReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public AutoPayFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(AutoPayFrequency frequency) {
        this.frequency = frequency;
    }

    public AutoPayStatus getStatus() {
        return status;
    }

    public void setStatus(AutoPayStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getNextExecutionTime() {
        return nextExecutionTime;
    }

    public void setNextExecutionTime(LocalDateTime nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
