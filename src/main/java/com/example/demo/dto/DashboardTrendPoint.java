package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Single time-series point for dashboard trend charts.")
public class DashboardTrendPoint {

    @Schema(description = "Label for the trend point, typically a date or period", example = "2026-07-31")
    private String label;

    @Schema(description = "Number of transactions in this period", example = "90")
    private long transactionCount;

    @Schema(description = "Successful transaction amount volume in this period", example = "125000.00")
    private BigDecimal transactionVolume;

    @Schema(description = "Success rate percentage for this period", example = "97.5")
    private double successRate;

    public DashboardTrendPoint() {
    }

    public DashboardTrendPoint(String label, long transactionCount, BigDecimal transactionVolume, double successRate) {
        this.label = label;
        this.transactionCount = transactionCount;
        this.transactionVolume = transactionVolume;
        this.successRate = successRate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTransactionVolume() {
        return transactionVolume;
    }

    public void setTransactionVolume(BigDecimal transactionVolume) {
        this.transactionVolume = transactionVolume;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }
}
