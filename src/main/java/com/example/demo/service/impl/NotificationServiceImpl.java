package com.example.demo.service.impl;

import com.example.demo.events.PaymentRequestCreatedEvent;
import com.example.demo.events.TransactionCompletedEvent;
import com.example.demo.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of NotificationService that simulates notification delivery
 * (SMS/Email/Push) via structured SLF4J logging.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void notifyTransactionCompleted(TransactionCompletedEvent event) {
        log.info("[NOTIFICATION SIMULATION] Transaction Completed Alert | " +
                        "reference: [{}] | sender: [{}] | receiver: [{}] | amount: [{}] | " +
                        "status: [{}] | eventTime: [{}] | correlationId: [{}]",
                event.getTransactionReference(),
                event.getSenderUpiId(),
                event.getReceiverUpiId(),
                event.getAmount(),
                event.getStatus(),
                event.getEventTime(),
                event.getCorrelationId());
    }

    @Override
    public void notifyPaymentRequestCreated(PaymentRequestCreatedEvent event) {
        log.info("[NOTIFICATION SIMULATION] Payment Request Created Alert | " +
                        "reference: [{}] | requester: [{}] | payer: [{}] | amount: [{}] | " +
                        "note: [{}] | expiresAt: [{}] | eventTime: [{}] | correlationId: [{}]",
                event.getRequestReference(),
                event.getReceiverUpiId(),
                event.getSenderUpiId(),
                event.getAmount(),
                event.getNote(),
                event.getExpiresAt(),
                event.getEventTime(),
                event.getCorrelationId());
    }
}
