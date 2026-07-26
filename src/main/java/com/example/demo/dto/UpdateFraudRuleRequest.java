package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Request payload for updating an existing fraud rule")
public class UpdateFraudRuleRequest {

    @Schema(description = "Updated description", example = "Flags transfers exceeding 75,000 INR")
    private String description;

    @Schema(description = "Enable or disable rule", example = "true")
    private Boolean enabled;

    @Schema(description = "Updated evaluation priority", example = "2")
    private Integer priority;

    @Schema(description = "Updated threshold amount", example = "75000.00")
    private BigDecimal thresholdAmount;

    @Schema(description = "Updated threshold count", example = "10")
    private Integer thresholdCount;

    @Schema(description = "Updated time window in minutes", example = "15")
    private Integer timeWindowMinutes;

    public UpdateFraudRuleRequest() {
    }

    public UpdateFraudRuleRequest(String description, Boolean enabled, Integer priority, 
                                 BigDecimal thresholdAmount, Integer thresholdCount, Integer timeWindowMinutes) {
        this.description = description;
        this.enabled = enabled;
        this.priority = priority;
        this.thresholdAmount = thresholdAmount;
        this.thresholdCount = thresholdCount;
        this.timeWindowMinutes = timeWindowMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public Integer getThresholdCount() {
        return thresholdCount;
    }

    public void setThresholdCount(Integer thresholdCount) {
        this.thresholdCount = thresholdCount;
    }

    public Integer getTimeWindowMinutes() {
        return timeWindowMinutes;
    }

    public void setTimeWindowMinutes(Integer timeWindowMinutes) {
        this.timeWindowMinutes = timeWindowMinutes;
    }
}
