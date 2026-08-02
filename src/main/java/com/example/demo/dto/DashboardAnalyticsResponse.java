package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Combined dashboard analytics for trend series and top performance metrics.")
public class DashboardAnalyticsResponse {

    @Schema(description = "Total transactions included in the analytics window", example = "1200")
    private long totalTransactions;

    @Schema(description = "Total transaction volume included in the analytics window", example = "1850000.50")
    private BigDecimal totalTransactionVolume;

    @Schema(description = "Time-series trend data for the dashboard analytics")
    private List<DashboardTrendPoint> trendSeries;

    @Schema(description = "Top merchant performance by transaction volume")
    private List<TopEntitySummary> topMerchants;

    @Schema(description = "Top user activity by transaction volume")
    private List<TopEntitySummary> topUsers;

    @Schema(description = "Top categories by transaction volume")
    private List<TopEntitySummary> topCategories;

    public DashboardAnalyticsResponse() {
    }

    public DashboardAnalyticsResponse(long totalTransactions,
                                      BigDecimal totalTransactionVolume,
                                      List<DashboardTrendPoint> trendSeries,
                                      List<TopEntitySummary> topMerchants,
                                      List<TopEntitySummary> topUsers,
                                      List<TopEntitySummary> topCategories) {
        this.totalTransactions = totalTransactions;
        this.totalTransactionVolume = totalTransactionVolume;
        this.trendSeries = trendSeries;
        this.topMerchants = topMerchants;
        this.topUsers = topUsers;
        this.topCategories = topCategories;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getTotalTransactionVolume() {
        return totalTransactionVolume;
    }

    public void setTotalTransactionVolume(BigDecimal totalTransactionVolume) {
        this.totalTransactionVolume = totalTransactionVolume;
    }

    public List<DashboardTrendPoint> getTrendSeries() {
        return trendSeries;
    }

    public void setTrendSeries(List<DashboardTrendPoint> trendSeries) {
        this.trendSeries = trendSeries;
    }

    public List<TopEntitySummary> getTopMerchants() {
        return topMerchants;
    }

    public void setTopMerchants(List<TopEntitySummary> topMerchants) {
        this.topMerchants = topMerchants;
    }

    public List<TopEntitySummary> getTopUsers() {
        return topUsers;
    }

    public void setTopUsers(List<TopEntitySummary> topUsers) {
        this.topUsers = topUsers;
    }

    public List<TopEntitySummary> getTopCategories() {
        return topCategories;
    }

    public void setTopCategories(List<TopEntitySummary> topCategories) {
        this.topCategories = topCategories;
    }
}
