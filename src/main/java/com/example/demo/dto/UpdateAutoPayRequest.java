package com.example.demo.dto;

import com.example.demo.entity.AutoPayFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request payload for updating an existing AutoPay mandate")
public class UpdateAutoPayRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Updated recurring transfer amount", example = "750.00")
    private BigDecimal amount;

    @Schema(description = "Updated frequency", example = "MONTHLY")
    private AutoPayFrequency frequency;

    @Schema(description = "Updated expiration end date", example = "2028-01-01")
    private LocalDate endDate;

    public UpdateAutoPayRequest() {
    }

    public UpdateAutoPayRequest(BigDecimal amount, AutoPayFrequency frequency, LocalDate endDate) {
        this.amount = amount;
        this.frequency = frequency;
        this.endDate = endDate;
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

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
