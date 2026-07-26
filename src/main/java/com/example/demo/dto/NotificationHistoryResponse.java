package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User notification feed and unread count wrapper")
public class NotificationHistoryResponse {

    @Schema(description = "List of user notifications")
    private List<NotificationResponse> notifications;

    @Schema(description = "Total unread count for user", example = "3")
    private long unreadCount;

    public NotificationHistoryResponse() {
    }

    public NotificationHistoryResponse(List<NotificationResponse> notifications, long unreadCount) {
        this.notifications = notifications;
        this.unreadCount = unreadCount;
    }

    public List<NotificationResponse> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
