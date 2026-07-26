package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response details for a merchant profile")
public class MerchantResponse {

    @Schema(description = "Merchant UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private UUID id;

    @Schema(description = "Full name of the merchant owner", example = "Jane Doe")
    private String merchantName;

    @Schema(description = "Registered business name", example = "Doe Retail Electronics")
    private String businessName;

    @Schema(description = "Unique merchant code", example = "MERCH-98765")
    private String merchantCode;

    @Schema(description = "Merchant UPI ID", example = "doeretail@upi")
    private String upiId;

    @Schema(description = "Business category / MCC", example = "RETAIL")
    private String category;

    @Schema(description = "Is merchant account active", example = "true")
    private Boolean active;

    @Schema(description = "Timestamp when merchant was registered")
    private LocalDateTime createdAt;

    public MerchantResponse() {
    }

    public MerchantResponse(UUID id, String merchantName, String businessName, String merchantCode, 
                            String upiId, String category, Boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.merchantName = merchantName;
        this.businessName = businessName;
        this.merchantCode = merchantCode;
        this.upiId = upiId;
        this.category = category;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
