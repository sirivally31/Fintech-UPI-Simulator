package com.example.demo.dto;

import com.example.demo.entity.NotificationChannel;
import com.example.demo.entity.NotificationStatus;
import com.example.demo.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Notification response details")
public class NotificationResponse {

    @Schema(description = "Notification UUID")
    private UUID id;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Notification type category", example = "PAYMENT_SUCCESS")
    private NotificationType type;

    @Schema(description = "Delivery channel", example = "IN_APP")
    private NotificationChannel channel;

    @Schema(description = "Title", example = "Payment Sent Successfully")
    private String title;

    @Schema(description = "Message content", example = "Rs. 500.00 transferred to Alice Smith.")
    private String message;

    @Schema(description = "Status (PENDING, SENT, FAILED, READ)", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "Priority rank", example = "2")
    private Integer priority;

    @Schema(description = "Reference ID", example = "TXN2026072618300001")
    private String referenceId;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Sent timestamp")
    private LocalDateTime sentAt;

    @Schema(description = "Read timestamp")
    private LocalDateTime readAt;

    public NotificationResponse() {
    }

    public NotificationResponse(UUID id, Long userId, NotificationType type, NotificationChannel channel, 
                                String title, String message, NotificationStatus status, Integer priority, 
                                String referenceId, LocalDateTime createdAt, LocalDateTime sentAt, LocalDateTime readAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.title = title;
        this.message = message;
        this.status = status;
        this.priority = priority;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.readAt = readAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
