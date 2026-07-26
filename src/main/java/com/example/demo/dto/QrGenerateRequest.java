package com.example.demo.dto;

import com.example.demo.entity.QRType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request payload for generating a static or dynamic Merchant QR Code")
public class QrGenerateRequest {

    @NotNull(message = "Merchant ID is required")
    @Schema(description = "UUID of the merchant generating the QR code")
    private UUID merchantId;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero for dynamic QR")
    @Schema(description = "Fixed transaction amount (required for DYNAMIC QR, optional for STATIC QR)", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Note/description associated with the QR payment", example = "Order #1042 Payment")
    private String description;

    @NotNull(message = "QR Type is required (STATIC or DYNAMIC)")
    @Schema(description = "Type of QR Code", example = "DYNAMIC")
    private QRType type;

    @Schema(description = "Expiration duration in minutes for DYNAMIC QR (default 15 minutes)", example = "15")
    private Long expiryMinutes;

    public QrGenerateRequest() {
    }

    public QrGenerateRequest(UUID merchantId, BigDecimal amount, String description, QRType type, Long expiryMinutes) {
        this.merchantId = merchantId;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.expiryMinutes = expiryMinutes;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
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

    public Long getExpiryMinutes() {
        return expiryMinutes;
    }

    public void setExpiryMinutes(Long expiryMinutes) {
        this.expiryMinutes = expiryMinutes;
    }
}
