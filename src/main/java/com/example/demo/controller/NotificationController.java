package com.example.demo.controller;

import com.example.demo.dto.NotificationResponse;
import com.example.demo.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification & Communication APIs", description = "Endpoints for retrieving user notification feeds, unread counts, and marking alerts as read")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get User Notifications", description = "Retrieves all notifications for the authenticated user ordered by newest first.")
    @ApiResponse(responseCode = "200", description = "List of notifications retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResponse.class))))
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        List<NotificationResponse> list = notificationService.getNotifications();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get Unread Count", description = "Retrieves unread notification count for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully")
    @GetMapping("/unread")
    public ResponseEntity<Long> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Mark Notification as Read", description = "Marks a specific notification as READ.")
    @ApiResponse(responseCode = "204", description = "Notification marked as read")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification UUID", required = true) @PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications for the authenticated user as READ.")
    @ApiResponse(responseCode = "204", description = "All notifications marked as read")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete Notification", description = "Deletes a specific notification for the authenticated user.")
    @ApiResponse(responseCode = "204", description = "Notification deleted successfully")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "Notification UUID", required = true) @PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
