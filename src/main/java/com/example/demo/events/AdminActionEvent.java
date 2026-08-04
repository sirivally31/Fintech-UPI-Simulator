package com.example.demo.events;

import java.time.LocalDateTime;

public class AdminActionEvent {
    private String adminUsername;
    private String action;
    private String details;
    private LocalDateTime timestamp;

    public AdminActionEvent() {}

    public AdminActionEvent(String adminUsername, String action, String details) {
        this.adminUsername = adminUsername;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
