package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Transaction operational analytics metrics")
public class TransactionAnalyticsResponse {

    @Schema(description = "Daily transaction count", example = "450")
    private long dailyCount;

    @Schema(description = "Weekly transaction count", example = "3150")
    private long weeklyCount;

    @Schema(description = "Monthly transaction count", example = "12500")
    private long monthlyCount;

    @Schema(description = "Daily transaction volume", example = "1500000.00")
    private BigDecimal dailyVolume;

    @Schema(description = "Success rate percentage", example = "96.8")
    private double successRate;

    @Schema(description = "Failure rate percentage", example = "3.2")
    private double failureRate;

    @Schema(description = "Average transaction payment amount", example = "3333.33")
    private BigDecimal averageAmount;

    @Schema(description = "Highest single payment amount processed", example = "100000.00")
    private BigDecimal highestAmount;

    public TransactionAnalyticsResponse() {
    }

    public TransactionAnalyticsResponse(long dailyCount, long weeklyCount, long monthlyCount, 
                                        BigDecimal dailyVolume, double successRate, double failureRate, 
                                        BigDecimal averageAmount, BigDecimal highestAmount) {
        this.dailyCount = dailyCount;
        this.weeklyCount = weeklyCount;
        this.monthlyCount = monthlyCount;
        this.dailyVolume = dailyVolume;
        this.successRate = successRate;
        this.failureRate = failureRate;
        this.averageAmount = averageAmount;
        this.highestAmount = highestAmount;
    }

    public long getDailyCount() {
        return dailyCount;
    }

    public void setDailyCount(long dailyCount) {
        this.dailyCount = dailyCount;
    }

    public long getWeeklyCount() {
        return weeklyCount;
    }

    public void setWeeklyCount(long weeklyCount) {
        this.weeklyCount = weeklyCount;
    }

    public long getMonthlyCount() {
        return monthlyCount;
    }

    public void setMonthlyCount(long monthlyCount) {
        this.monthlyCount = monthlyCount;
    }

    public BigDecimal getDailyVolume() {
        return dailyVolume;
    }

    public void setDailyVolume(BigDecimal dailyVolume) {
        this.dailyVolume = dailyVolume;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }

    public BigDecimal getHighestAmount() {
        return highestAmount;
    }

    public void setHighestAmount(BigDecimal highestAmount) {
        this.highestAmount = highestAmount;
    }
}
