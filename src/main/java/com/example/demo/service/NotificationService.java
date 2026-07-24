package com.example.demo.service;

import com.example.demo.events.PaymentRequestCreatedEvent;
import com.example.demo.events.TransactionCompletedEvent;

/**
 * Service contract for handling user and system notifications triggered by domain events.
 */
public interface NotificationService {

    void notifyTransactionCompleted(TransactionCompletedEvent event);

    void notifyPaymentRequestCreated(PaymentRequestCreatedEvent event);
}
