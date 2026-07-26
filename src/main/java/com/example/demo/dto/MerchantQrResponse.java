package com.example.demo.dto;

import com.example.demo.entity.QRType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response details for a generated Merchant QR Code")
public class MerchantQrResponse {

    @Schema(description = "QR Entity UUID")
    private UUID id;

    @Schema(description = "Merchant UUID")
    private UUID merchantId;

    @Schema(description = "Merchant Name", example = "Jane Doe")
    private String merchantName;

    @Schema(description = "Merchant Business Name", example = "Doe Retail Electronics")
    private String businessName;

    @Schema(description = "Merchant UPI ID", example = "doeretail@upi")
    private String upiId;

    @Schema(description = "Unique Secure QR Token UUID", example = "qr_9b1deb4d-3b7d-4b69-9175-2244668800aa")
    private String qrToken;

    @Schema(description = "Fixed transaction amount (if applicable)", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Payment description / note", example = "Order Payment")
    private String description;

    @Schema(description = "QR Type (STATIC or DYNAMIC)", example = "DYNAMIC")
    private QRType type;

    @Schema(description = "Whether the QR code has been used", example = "false")
    private Boolean used;

    @Schema(description = "Expiration timestamp for DYNAMIC QR (null for STATIC)")
    private LocalDateTime expiryTime;

    @Schema(description = "Standard NPCI / UPI payment URI format", example = "upi://pay?pa=doeretail@upi&pn=Doe%20Retail%20Electronics&am=250.00&cu=INR&tn=Order%20Payment")
    private String paymentUri;

    @Schema(description = "Timestamp when QR was created")
    private LocalDateTime createdAt;

    public MerchantQrResponse() {
    }

    public MerchantQrResponse(UUID id, UUID merchantId, String merchantName, String businessName, 
                              String upiId, String qrToken, BigDecimal amount, String description, 
                              QRType type, Boolean used, LocalDateTime expiryTime, 
                              String paymentUri, LocalDateTime createdAt) {
        this.id = id;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.businessName = businessName;
        this.upiId = upiId;
        this.qrToken = qrToken;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.used = used;
        this.expiryTime = expiryTime;
        this.paymentUri = paymentUri;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
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

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getPaymentUri() {
        return paymentUri;
    }

    public void setPaymentUri(String paymentUri) {
        this.paymentUri = paymentUri;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
