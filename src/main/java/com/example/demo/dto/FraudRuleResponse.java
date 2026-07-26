package com.example.demo.dto;

import com.example.demo.entity.FraudRuleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response object representing a fraud rule profile")
public class FraudRuleResponse {

    @Schema(description = "Fraud rule UUID")
    private UUID id;

    @Schema(description = "Rule name", example = "High Value Transfer Guard")
    private String ruleName;

    @Schema(description = "Rule description")
    private String description;

    @Schema(description = "Rule enabled status", example = "true")
    private Boolean enabled;

    @Schema(description = "Rule priority rank", example = "1")
    private Integer priority;

    @Schema(description = "Rule type", example = "HIGH_VALUE_TRANSACTION")
    private FraudRuleType type;

    @Schema(description = "Threshold amount")
    private BigDecimal thresholdAmount;

    @Schema(description = "Threshold count")
    private Integer thresholdCount;

    @Schema(description = "Time window in minutes")
    private Integer timeWindowMinutes;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    private LocalDateTime updatedAt;

    public FraudRuleResponse() {
    }

    public FraudRuleResponse(UUID id, String ruleName, String description, Boolean enabled, Integer priority, 
                             FraudRuleType type, BigDecimal thresholdAmount, Integer thresholdCount, 
                             Integer timeWindowMinutes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ruleName = ruleName;
        this.description = description;
        this.enabled = enabled;
        this.priority = priority;
        this.type = type;
        this.thresholdAmount = thresholdAmount;
        this.thresholdCount = thresholdCount;
        this.timeWindowMinutes = timeWindowMinutes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
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

    public FraudRuleType getType() {
        return type;
    }

    public void setType(FraudRuleType type) {
        this.type = type;
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
