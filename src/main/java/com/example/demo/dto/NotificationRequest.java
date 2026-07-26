package com.example.demo.dto;

import com.example.demo.entity.NotificationChannel;
import com.example.demo.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for creating/dispatching a notification")
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "Target User ID", example = "1")
    private Long userId;

    @NotNull(message = "Notification type is required")
    @Schema(description = "Type category", example = "PAYMENT_SUCCESS")
    private NotificationType type;

    @NotNull(message = "Delivery channel is required")
    @Schema(description = "Delivery channel (EMAIL, SMS, PUSH, IN_APP)", example = "IN_APP")
    private NotificationChannel channel;

    @NotBlank(message = "Title is required")
    @Schema(description = "Notification title", example = "Payment Sent Successfully")
    private String title;

    @NotBlank(message = "Message body is required")
    @Schema(description = "Notification content body", example = "Rs. 500.00 transferred to Alice Smith.")
    private String message;

    @Schema(description = "Priority rank (1=High, 2=Normal, 3=Low)", example = "2")
    private Integer priority = 2;

    @Schema(description = "Associated transaction or mandate reference ID", example = "TXN2026072618300001")
    private String referenceId;

    public NotificationRequest() {
    }

    public NotificationRequest(Long userId, NotificationType type, NotificationChannel channel, 
                               String title, String message, Integer priority, String referenceId) {
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.referenceId = referenceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
