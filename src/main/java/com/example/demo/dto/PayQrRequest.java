package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload for executing a QR Payment")
public class PayQrRequest {

    @NotBlank(message = "Payer UPI ID is required")
    @Schema(description = "UPI ID of the payer initiating the payment", example = "john@upi")
    private String payerUpiId;

    @NotBlank(message = "QR Token is required")
    @Schema(description = "Scanned QR token string", example = "qr_9b1deb4d-3b7d-4b69-9175-2244668800aa")
    private String qrToken;

    @NotBlank(message = "UPI PIN is required")
    @Schema(description = "4 or 6 digit UPI PIN for authorization", example = "1234")
    private String upiPin;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Payment amount to transfer", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Optional client idempotency key or reference for duplicate prevention", example = "IDEM-882910-KEY")
    private String idempotencyKey;

    public PayQrRequest() {
    }

    public PayQrRequest(String payerUpiId, String qrToken, String upiPin, BigDecimal amount, String idempotencyKey) {
        this.payerUpiId = payerUpiId;
        this.qrToken = qrToken;
        this.upiPin = upiPin;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
    }

    public String getPayerUpiId() {
        return payerUpiId;
    }

    public void setPayerUpiId(String payerUpiId) {
        this.payerUpiId = payerUpiId;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getUpiPin() {
        return upiPin;
    }

    public void setUpiPin(String upiPin) {
        this.upiPin = upiPin;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
