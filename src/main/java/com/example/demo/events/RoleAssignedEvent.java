package com.example.demo.events;

import java.time.LocalDateTime;

public class RoleAssignedEvent {
    private String adminUsername;
    private String targetUsername;
    private String roleName;
    private LocalDateTime timestamp;

    public RoleAssignedEvent() {}

    public RoleAssignedEvent(String adminUsername, String targetUsername, String roleName) {
        this.adminUsername = adminUsername;
        this.targetUsername = targetUsername;
        this.roleName = roleName;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
