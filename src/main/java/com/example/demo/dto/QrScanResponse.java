package com.example.demo.dto;

import com.example.demo.entity.QRType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response details returned after scanning a Merchant QR Code")
public class QrScanResponse {

    @Schema(description = "Scanned QR token", example = "qr_9b1deb4d-3b7d-4b69-9175-2244668800aa")
    private String qrToken;

    @Schema(description = "Merchant UUID")
    private UUID merchantId;

    @Schema(description = "Merchant Owner Name", example = "Jane Doe")
    private String merchantName;

    @Schema(description = "Merchant Business Name", example = "Doe Retail Electronics")
    private String businessName;

    @Schema(description = "Merchant UPI ID for payment transfer", example = "doeretail@upi")
    private String upiId;

    @Schema(description = "Preset payment amount (if DYNAMIC or fixed STATIC)", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Payment note or description", example = "Order Payment")
    private String description;

    @Schema(description = "QR Type (STATIC or DYNAMIC)", example = "DYNAMIC")
    private QRType type;

    @Schema(description = "Indicates if QR is active and valid for payment", example = "true")
    private Boolean valid;

    @Schema(description = "Indicates if QR has expired", example = "false")
    private Boolean expired;

    @Schema(description = "Indicates if dynamic QR has already been used", example = "false")
    private Boolean used;

    @Schema(description = "Standard UPI URI", example = "upi://pay?pa=doeretail@upi&pn=Doe%20Retail%20Electronics&am=250.00&cu=INR&tn=Order%20Payment")
    private String paymentUri;

    @Schema(description = "Validation or status message", example = "QR Code successfully scanned and valid for payment")
    private String message;

    public QrScanResponse() {
    }

    public QrScanResponse(String qrToken, UUID merchantId, String merchantName, String businessName, 
                          String upiId, BigDecimal amount, String description, QRType type, 
                          Boolean valid, Boolean expired, Boolean used, String paymentUri, String message) {
        this.qrToken = qrToken;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.businessName = businessName;
        this.upiId = upiId;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.valid = valid;
        this.expired = expired;
        this.used = used;
        this.paymentUri = paymentUri;
        this.message = message;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
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

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QRType getType() {
        return type;
    }

    public void setType(QRType type) {
        this.type = type;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public String getPaymentUri() {
        return paymentUri;
    }

    public void setPaymentUri(String paymentUri) {
        this.paymentUri = paymentUri;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
