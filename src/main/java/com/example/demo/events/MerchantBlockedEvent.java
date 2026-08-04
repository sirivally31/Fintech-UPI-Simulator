package com.example.demo.events;

import java.time.LocalDateTime;

public class MerchantBlockedEvent {
    private String merchantCode;
    private String blockedBy;
    private String reason;
    private LocalDateTime timestamp;

    public MerchantBlockedEvent() {}

    public MerchantBlockedEvent(String merchantCode, String blockedBy, String reason) {
        this.merchantCode = merchantCode;
        this.blockedBy = blockedBy;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { this.merchantCode = merchantCode; }
    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
