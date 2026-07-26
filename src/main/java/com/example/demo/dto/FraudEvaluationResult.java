package com.example.demo.dto;

import com.example.demo.entity.FraudDecision;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Risk Engine evaluation result for a transaction")
public class FraudEvaluationResult {

    @Schema(description = "Calculated risk score (0 to 100)", example = "75")
    private int riskScore;

    @Schema(description = "Decision (ALLOW, REVIEW, BLOCK)", example = "BLOCK")
    private FraudDecision decision;

    @Schema(description = "List of triggered rule names")
    private List<String> triggeredRules;

    @Schema(description = "Evaluation timestamp")
    private LocalDateTime evaluationTime;

    public FraudEvaluationResult() {
    }

    public FraudEvaluationResult(int riskScore, FraudDecision decision, List<String> triggeredRules, LocalDateTime evaluationTime) {
        this.riskScore = riskScore;
        this.decision = decision;
        this.triggeredRules = triggeredRules;
        this.evaluationTime = evaluationTime;
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

    public LocalDateTime getEvaluationTime() {
        return evaluationTime;
    }

    public void setEvaluationTime(LocalDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }
}
