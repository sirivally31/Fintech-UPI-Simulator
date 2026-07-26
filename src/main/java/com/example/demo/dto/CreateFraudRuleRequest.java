package com.example.demo.dto;

import com.example.demo.entity.FraudRuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload for defining a new fraud rule")
public class CreateFraudRuleRequest {

    @NotBlank(message = "Rule name is required")
    @Schema(description = "Unique rule name", example = "High Value Transfer Guard")
    private String ruleName;

    @NotBlank(message = "Description is required")
    @Schema(description = "Rule description", example = "Flags transfers over 50,000 INR")
    private String description;

    @NotNull(message = "Rule type is required")
    @Schema(description = "Fraud rule detection type", example = "HIGH_VALUE_TRANSACTION")
    private FraudRuleType type;

    @Schema(description = "Priority rank (lower is evaluated first)", example = "1")
    @Min(value = 1, message = "Priority must be at least 1")
    private Integer priority = 1;

    @Schema(description = "Threshold amount", example = "50000.00")
    private BigDecimal thresholdAmount;

    @Schema(description = "Threshold count", example = "5")
    private Integer thresholdCount;

    @Schema(description = "Time window in minutes", example = "10")
    private Integer timeWindowMinutes;

    public CreateFraudRuleRequest() {
    }

    public CreateFraudRuleRequest(String ruleName, String description, FraudRuleType type, Integer priority, 
                                 BigDecimal thresholdAmount, Integer thresholdCount, Integer timeWindowMinutes) {
        this.ruleName = ruleName;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.thresholdAmount = thresholdAmount;
        this.thresholdCount = thresholdCount;
        this.timeWindowMinutes = timeWindowMinutes;
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

    public FraudRuleType getType() {
        return type;
    }

    public void setType(FraudRuleType type) {
        this.type = type;
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
