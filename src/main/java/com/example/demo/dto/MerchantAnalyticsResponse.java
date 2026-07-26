package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Merchant analytics summary for operations team")
public class MerchantAnalyticsResponse {

    @Schema(description = "Total registered merchants", example = "45")
    private long totalMerchants;

    @Schema(description = "Active merchants count", example = "40")
    private long activeMerchants;

    @Schema(description = "Pending verification merchant count", example = "5")
    private long pendingMerchants;

    @Schema(description = "Most active merchant business name", example = "SuperMart Retail")
    private String mostActiveMerchantName;

    public MerchantAnalyticsResponse() {
    }

    public MerchantAnalyticsResponse(long totalMerchants, long activeMerchants, long pendingMerchants, String mostActiveMerchantName) {
        this.totalMerchants = totalMerchants;
        this.activeMerchants = activeMerchants;
        this.pendingMerchants = pendingMerchants;
        this.mostActiveMerchantName = mostActiveMerchantName;
    }

    public long getTotalMerchants() {
        return totalMerchants;
    }

    public void setTotalMerchants(long totalMerchants) {
        this.totalMerchants = totalMerchants;
    }

    public long getActiveMerchants() {
        return activeMerchants;
    }

    public void setActiveMerchants(long activeMerchants) {
        this.activeMerchants = activeMerchants;
    }

    public long getPendingMerchants() {
        return pendingMerchants;
    }

    public void setPendingMerchants(long pendingMerchants) {
        this.pendingMerchants = pendingMerchants;
    }

    public String getMostActiveMerchantName() {
        return mostActiveMerchantName;
    }

    public void setMostActiveMerchantName(String mostActiveMerchantName) {
        this.mostActiveMerchantName = mostActiveMerchantName;
    }
}
