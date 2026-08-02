package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Summary record for a top-performing entity in dashboard analytics.")
public class TopEntitySummary {

    @Schema(description = "Entity type, such as MERCHANT, USER or CATEGORY", example = "MERCHANT")
    private String entityType;

    @Schema(description = "Name of the entity", example = "SuperMart Retail")
    private String entityName;

    @Schema(description = "Category or group for the entity, if applicable", example = "Retail")
    private String category;

    @Schema(description = "Number of transactions attributed to the entity", example = "120")
    private long transactionCount;

    @Schema(description = "Total transaction volume attributed to the entity", example = "450000.00")
    private BigDecimal transactionVolume;

    public TopEntitySummary() {
    }

    public TopEntitySummary(String entityType, String entityName, String category, long transactionCount, BigDecimal transactionVolume) {
        this.entityType = entityType;
        this.entityName = entityName;
        this.category = category;
        this.transactionCount = transactionCount;
        this.transactionVolume = transactionVolume;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}
