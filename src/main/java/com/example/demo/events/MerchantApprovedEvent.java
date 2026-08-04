package com.example.demo.events;

import java.time.LocalDateTime;

public class MerchantApprovedEvent {
    private String merchantCode;
    private String approvedBy;
    private LocalDateTime timestamp;

    public MerchantApprovedEvent() {}

    public MerchantApprovedEvent(String merchantCode, String approvedBy) {
        this.merchantCode = merchantCode;
        this.approvedBy = approvedBy;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { this.merchantCode = merchantCode; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
