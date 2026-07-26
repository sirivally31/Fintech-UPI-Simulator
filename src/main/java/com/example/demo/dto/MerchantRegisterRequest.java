package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for registering a new merchant")
public class MerchantRegisterRequest {

    @NotBlank(message = "Merchant name is required")
    @Size(min = 2, max = 100, message = "Merchant name must be between 2 and 100 characters")
    @Schema(description = "Full name of the merchant", example = "Jane Doe")
    private String merchantName;

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    @Schema(description = "Registered business name", example = "Doe Retail Electronics")
    private String businessName;

    @NotBlank(message = "Merchant code is required")
    @Size(min = 3, max = 50, message = "Merchant code must be between 3 and 50 characters")
    @Schema(description = "Unique identifier code for the merchant", example = "MERCH-98765")
    private String merchantCode;

    @NotBlank(message = "UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$", message = "Invalid UPI ID format")
    @Schema(description = "Merchant's unique UPI ID for receiving payments", example = "doeretail@upi")
    private String upiId;

    @NotBlank(message = "Category is required")
    @Schema(description = "Merchant business category code / MCC", example = "RETAIL")
    private String category;

    public MerchantRegisterRequest() {
    }

    public MerchantRegisterRequest(String merchantName, String businessName, String merchantCode, String upiId, String category) {
        this.merchantName = merchantName;
        this.businessName = businessName;
        this.merchantCode = merchantCode;
        this.upiId = upiId;
        this.category = category;
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
}
