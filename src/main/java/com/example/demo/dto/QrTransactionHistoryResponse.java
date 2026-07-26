package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Response payload for QR Payment transaction history")
public class QrTransactionHistoryResponse {

    @Schema(description = "Transaction ID", example = "104")
    private Long transactionId;

    @Schema(description = "Transaction reference", example = "TXN202607261730000001")
    private String transactionReference;

    @Schema(description = "Merchant Owner Name", example = "Jane Doe")
    private String merchantName;

    @Schema(description = "Merchant Business Name", example = "Doe Retail Electronics")
    private String merchantBusinessName;

    @Schema(description = "Merchant UPI ID", example = "doeretail@upi")
    private String merchantUpiId;

    @Schema(description = "Payer UPI ID", example = "john@upi")
    private String payerUpiId;

    @Schema(description = "Amount", example = "250.00")
    private BigDecimal amount;

    @Schema(description = "Status", example = "SUCCESS")
    private String status;

    @Schema(description = "UTR Number", example = "UTR2026072699887766")
    private String utr;

    @Schema(description = "QR Type (STATIC or DYNAMIC)", example = "DYNAMIC")
    private String qrType;

    @Schema(description = "Transaction timestamp")
    private LocalDateTime timestamp;

    public QrTransactionHistoryResponse() {
    }

    public QrTransactionHistoryResponse(Long transactionId, String transactionReference, 
                                        String merchantName, String merchantBusinessName, 
                                        String merchantUpiId, String payerUpiId, 
                                        BigDecimal amount, String status, String utr, 
                                        String qrType, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.transactionReference = transactionReference;
        this.merchantName = merchantName;
        this.merchantBusinessName = merchantBusinessName;
        this.merchantUpiId = merchantUpiId;
        this.payerUpiId = payerUpiId;
        this.amount = amount;
        this.status = status;
        this.utr = utr;
        this.qrType = qrType;
        this.timestamp = timestamp;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantBusinessName() {
        return merchantBusinessName;
    }

    public void setMerchantBusinessName(String merchantBusinessName) {
        this.merchantBusinessName = merchantBusinessName;
    }

    public String getMerchantUpiId() {
        return merchantUpiId;
    }

    public void setMerchantUpiId(String merchantUpiId) {
        this.merchantUpiId = merchantUpiId;
    }

    public String getPayerUpiId() {
        return payerUpiId;
    }

    public void setPayerUpiId(String payerUpiId) {
        this.payerUpiId = payerUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUtr() {
        return utr;
    }

    public void setUtr(String utr) {
        this.utr = utr;
    }

    public String getQrType() {
        return qrType;
    }

    public void setQrType(String qrType) {
        this.qrType = qrType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
