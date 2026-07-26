package com.example.demo.dto;

import com.example.demo.entity.FraudDecision;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Fraud log entry for audit and risk analysis")
public class FraudLogResponse {

    @Schema(description = "Event UUID")
    private UUID id;

    @Schema(description = "Payer UPI ID", example = "john@upi")
    private String payerUpiId;

    @Schema(description = "Payee UPI ID", example = "alice@upi")
    private String payeeUpiId;

    @Schema(description = "Amount evaluated", example = "75000.00")
    private BigDecimal amount;

    @Schema(description = "Calculated risk score", example = "85")
    private int riskScore;

    @Schema(description = "Decision (ALLOW, REVIEW, BLOCK)", example = "BLOCK")
    private FraudDecision decision;

    @Schema(description = "Triggered rule names")
    private List<String> triggeredRules;

    @Schema(description = "Evaluation timestamp")
    private LocalDateTime timestamp;

    public FraudLogResponse() {
    }

    public FraudLogResponse(UUID id, String payerUpiId, String payeeUpiId, BigDecimal amount, 
                            int riskScore, FraudDecision decision, List<String> triggeredRules, LocalDateTime timestamp) {
        this.id = id;
        this.payerUpiId = payerUpiId;
        this.payeeUpiId = payeeUpiId;
        this.amount = amount;
        this.riskScore = riskScore;
        this.decision = decision;
        this.triggeredRules = triggeredRules;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPayerUpiId() {
        return payerUpiId;
    }

    public void setPayerUpiId(String payerUpiId) {
        this.payerUpiId = payerUpiId;
    }

    public String getPayeeUpiId() {
        return payeeUpiId;
    }

    public void setPayeeUpiId(String payeeUpiId) {
        this.payeeUpiId = payeeUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public FraudDecision getDecision() {
        return decision;
    }

    public void setDecision(FraudDecision decision) {
        this.decision = decision;
    }

    public List<String> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<String> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
