package com.example.demo.events;

import java.time.LocalDateTime;

public class SystemConfigUpdatedEvent {
    private String configKey;
    private String newValue;
    private String updatedBy;
    private LocalDateTime timestamp;

    public SystemConfigUpdatedEvent() {}

    public SystemConfigUpdatedEvent(String configKey, String newValue, String updatedBy) {
        this.configKey = configKey;
        this.newValue = newValue;
        this.updatedBy = updatedBy;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
