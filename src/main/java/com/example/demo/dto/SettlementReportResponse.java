package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Financial settlement performance report")
public class SettlementReportResponse {

    @Schema(description = "Report timeframe period (DAILY, WEEKLY, MONTHLY)", example = "DAILY")
    private String reportPeriod;

    @Schema(description = "Total batches generated", example = "10")
    private long totalBatches;

    @Schema(description = "Completed batches", example = "10")
    private long completedBatches;

    @Schema(description = "Failed batches", example = "0")
    private long failedBatches;

    @Schema(description = "Total settled monetary volume", example = "1500000.00")
    private BigDecimal totalSettledVolume;

    @Schema(description = "Total interchange/platform fees collected", example = "0.00")
    private BigDecimal totalFeesCollected;

    @Schema(description = "Reconciliation Summary")
    private ReconciliationSummaryResponse reconciliationSummary;

    @Schema(description = "Report generation timestamp")
    private LocalDateTime generatedAt;

    public SettlementReportResponse() {
    }

    public SettlementReportResponse(String reportPeriod, long totalBatches, long completedBatches, 
                                    long failedBatches, BigDecimal totalSettledVolume, BigDecimal totalFeesCollected, 
                                    ReconciliationSummaryResponse reconciliationSummary, LocalDateTime generatedAt) {
        this.reportPeriod = reportPeriod;
        this.totalBatches = totalBatches;
        this.completedBatches = completedBatches;
        this.failedBatches = failedBatches;
        this.totalSettledVolume = totalSettledVolume;
        this.totalFeesCollected = totalFeesCollected;
        this.reconciliationSummary = reconciliationSummary;
        this.generatedAt = generatedAt;
    }

    public String getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(String reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    public long getTotalBatches() {
        return totalBatches;
    }

    public void setTotalBatches(long totalBatches) {
        this.totalBatches = totalBatches;
    }

    public long getCompletedBatches() {
        return completedBatches;
    }

    public void setCompletedBatches(long completedBatches) {
        this.completedBatches = completedBatches;
    }

    public long getFailedBatches() {
        return failedBatches;
    }

    public void setFailedBatches(long failedBatches) {
        this.failedBatches = failedBatches;
    }

    public BigDecimal getTotalSettledVolume() {
        return totalSettledVolume;
    }

    public void setTotalSettledVolume(BigDecimal totalSettledVolume) {
        this.totalSettledVolume = totalSettledVolume;
    }

    public BigDecimal getTotalFeesCollected() {
        return totalFeesCollected;
    }

    public void setTotalFeesCollected(BigDecimal totalFeesCollected) {
        this.totalFeesCollected = totalFeesCollected;
    }

    public ReconciliationSummaryResponse getReconciliationSummary() {
        return reconciliationSummary;
    }

    public void setReconciliationSummary(ReconciliationSummaryResponse reconciliationSummary) {
        this.reconciliationSummary = reconciliationSummary;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
