package com.example.demo.service;

import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.events.PaymentRequestCreatedEvent;
import com.example.demo.events.TransactionCompletedEvent;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for handling user and system notifications triggered by domain events or REST API calls.
 */
public interface NotificationService {

    void notifyTransactionCompleted(TransactionCompletedEvent event);

    void notifyPaymentRequestCreated(PaymentRequestCreatedEvent event);

    NotificationResponse createNotification(NotificationRequest request);

    void markAsRead(UUID id);

    void markAllAsRead();

    List<NotificationResponse> getNotifications();

    long getUnreadCount();

    void deleteNotification(UUID id);
}
