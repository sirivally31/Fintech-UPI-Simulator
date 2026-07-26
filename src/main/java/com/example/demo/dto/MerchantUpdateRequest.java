package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating merchant details")
public class MerchantUpdateRequest {

    @Size(min = 2, max = 100, message = "Merchant name must be between 2 and 100 characters")
    @Schema(description = "Updated owner name", example = "Jane Smith")
    private String merchantName;

    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    @Schema(description = "Updated business name", example = "Smith Retail Superstore")
    private String businessName;

    @Schema(description = "Updated category", example = "GROCERY")
    private String category;

    @Schema(description = "Activate or deactivate merchant status", example = "true")
    private Boolean active;

    public MerchantUpdateRequest() {
    }

    public MerchantUpdateRequest(String merchantName, String businessName, String category, Boolean active) {
        this.merchantName = merchantName;
        this.businessName = businessName;
        this.category = category;
        this.active = active;
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
}
