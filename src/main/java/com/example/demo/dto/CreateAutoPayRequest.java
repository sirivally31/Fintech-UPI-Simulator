package com.example.demo.dto;

import com.example.demo.entity.AutoPayFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request payload for setting up a new AutoPay mandate")
public class CreateAutoPayRequest {

    @NotNull(message = "Beneficiary ID is required")
    @Schema(description = "UUID of the saved beneficiary", example = "f47ac10b-58cc-4372-a567-0e02b2c3d4e5")
    private UUID beneficiaryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Recurring transfer amount per execution", example = "500.00")
    private BigDecimal amount;

    @NotNull(message = "Frequency is required")
    @Schema(description = "Recurring frequency (DAILY, WEEKLY, MONTHLY, YEARLY)", example = "MONTHLY")
    private AutoPayFrequency frequency;

    @NotNull(message = "Start date is required")
    @Schema(description = "Mandate start date", example = "2026-08-01")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "Mandate expiration end date", example = "2027-08-01")
    private LocalDate endDate;

    public CreateAutoPayRequest() {
    }

    public CreateAutoPayRequest(UUID beneficiaryId, BigDecimal amount, AutoPayFrequency frequency, LocalDate startDate, LocalDate endDate) {
        this.beneficiaryId = beneficiaryId;
        this.amount = amount;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(UUID beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
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
}
