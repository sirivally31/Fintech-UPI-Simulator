package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fraud risk engine analytics summary")
public class FraudAnalyticsResponse {

    @Schema(description = "Total risk evaluations", example = "12500")
    private long totalEvaluations;

    @Schema(description = "Total transactions blocked by risk engine", example = "15")
    private long totalBlocked;

    @Schema(description = "Total transactions flagged for review", example = "42")
    private long totalReviewed;

    @Schema(description = "Total allowed transactions", example = "12443")
    private long totalAllowed;

    public FraudAnalyticsResponse() {
    }

    public FraudAnalyticsResponse(long totalEvaluations, long totalBlocked, long totalReviewed, long totalAllowed) {
        this.totalEvaluations = totalEvaluations;
        this.totalBlocked = totalBlocked;
        this.totalReviewed = totalReviewed;
        this.totalAllowed = totalAllowed;
    }

    public long getTotalEvaluations() {
        return totalEvaluations;
    }

    public void setTotalEvaluations(long totalEvaluations) {
        this.totalEvaluations = totalEvaluations;
    }

    public long getTotalBlocked() {
        return totalBlocked;
    }

    public void setTotalBlocked(long totalBlocked) {
        this.totalBlocked = totalBlocked;
    }

    public long getTotalReviewed() {
        return totalReviewed;
    }

    public void setTotalReviewed(long totalReviewed) {
        this.totalReviewed = totalReviewed;
    }

    public long getTotalAllowed() {
        return totalAllowed;
    }

    public void setTotalAllowed(long totalAllowed) {
        this.totalAllowed = totalAllowed;
    }
}
