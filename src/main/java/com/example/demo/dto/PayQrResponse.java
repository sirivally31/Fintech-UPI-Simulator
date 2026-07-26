package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Response details for QR Payment execution")
public class PayQrResponse {

    @Schema(description = "Unique internal transaction reference", example = "TXN202607261730000001")
    private String transactionReference;

    @Schema(description = "Bank UTR Number", example = "UTR2026072699887766")
    private String utrNumber;

    @Schema(description = "Payer UPI ID", example = "john@upi")
    private String payerUpiId;

    @Schema(description = "Merchant UPI ID", example = "doeretail@upi")
    private String merchantUpiId;

    @Schema(description = "Merchant Business Name", example = "Doe Retail Electronics")
    private String merchantBusinessName;

    @Schema(description = "Transaction Amount", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Payment Result status enum", example = "SUCCESS")
    private PaymentResult result;

    @Schema(description = "Transaction Status", example = "SUCCESS")
    private String status;

    @Schema(description = "Descriptive message", example = "Payment completed successfully")
    private String message;

    @Schema(description = "Timestamp of payment completion")
    private LocalDateTime timestamp;

    public PayQrResponse() {
    }

    public PayQrResponse(String transactionReference, String utrNumber, String payerUpiId, 
                        String merchantUpiId, String merchantBusinessName, BigDecimal amount, 
                        PaymentResult result, String status, String message, LocalDateTime timestamp) {
        this.transactionReference = transactionReference;
        this.utrNumber = utrNumber;
        this.payerUpiId = payerUpiId;
        this.merchantUpiId = merchantUpiId;
        this.merchantBusinessName = merchantBusinessName;
        this.amount = amount;
        this.result = result;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getUtrNumber() {
        return utrNumber;
    }

    public void setUtrNumber(String utrNumber) {
        this.utrNumber = utrNumber;
    }

    public String getPayerUpiId() {
        return payerUpiId;
    }

    public void setPayerUpiId(String payerUpiId) {
        this.payerUpiId = payerUpiId;
    }

    public String getMerchantUpiId() {
        return merchantUpiId;
    }

    public void setMerchantUpiId(String merchantUpiId) {
        this.merchantUpiId = merchantUpiId;
    }

    public String getMerchantBusinessName() {
        return merchantBusinessName;
    }

    public void setMerchantBusinessName(String merchantBusinessName) {
        this.merchantBusinessName = merchantBusinessName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentResult getResult() {
        return result;
    }

    public void setResult(PaymentResult result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
