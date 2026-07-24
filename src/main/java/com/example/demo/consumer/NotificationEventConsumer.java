package com.example.demo.consumer;

import com.example.demo.config.KafkaTopics;
import com.example.demo.events.PaymentRequestCreatedEvent;
import com.example.demo.events.TransactionCompletedEvent;
import com.example.demo.service.IdempotencyService;
import com.example.demo.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer responsible for receiving domain events, verifying idempotency,
 * and delegating notification processing to the NotificationService.
 *
 * <p>Design Principle: Consumer methods remain thin wrappers that log reception,
 * check idempotency, and delegate to service layer without containing domain business logic.</p>
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final IdempotencyService idempotencyService;

    public NotificationEventConsumer(NotificationService notificationService,
                                      IdempotencyService idempotencyService) {
        this.notificationService = notificationService;
        this.idempotencyService = idempotencyService;
    }

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_COMPLETED,
            groupId = "${spring.kafka.consumer.group-id:upi-simulator-group}"
    )
    public void consumeTransactionCompleted(TransactionCompletedEvent event) {
        try {
            if (event == null) {
                log.warn("Received null TransactionCompletedEvent from topic [{}]", KafkaTopics.TRANSACTION_COMPLETED);
                return;
            }

            if (idempotencyService.isEventProcessed(event.getEventId())) {
                log.info("Duplicate event detected, skipping processing | eventId: [{}] | reference: [{}] | correlationId: [{}]",
                        event.getEventId(), event.getTransactionReference(), event.getCorrelationId());
                return;
            }

            log.info("Received TransactionCompletedEvent from topic [{}] | reference: [{}] | correlationId: [{}]",
                    KafkaTopics.TRANSACTION_COMPLETED,
                    event.getTransactionReference(),
                    event.getCorrelationId());

            notificationService.notifyTransactionCompleted(event);
            idempotencyService.markEventProcessed(event.getEventId(), event.getEventType(), event.getCorrelationId());
        } catch (Exception e) {
            log.error("Error processing TransactionCompletedEvent | reference: [{}] | correlationId: [{}]",
                    event != null ? event.getTransactionReference() : "unknown",
                    event != null ? event.getCorrelationId() : "unknown", e);
            throw e; // Rethrow exception so Spring Kafka retry and DLT mechanisms are triggered
        }
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_REQUEST_CREATED,
            groupId = "${spring.kafka.consumer.group-id:upi-simulator-group}"
    )
    public void consumePaymentRequestCreated(PaymentRequestCreatedEvent event) {
        try {
            if (event == null) {
                log.warn("Received null PaymentRequestCreatedEvent from topic [{}]", KafkaTopics.PAYMENT_REQUEST_CREATED);
                return;
            }

            if (idempotencyService.isEventProcessed(event.getEventId())) {
                log.info("Duplicate event detected, skipping processing | eventId: [{}] | reference: [{}] | correlationId: [{}]",
                        event.getEventId(), event.getRequestReference(), event.getCorrelationId());
                return;
            }

            log.info("Received PaymentRequestCreatedEvent from topic [{}] | reference: [{}] | correlationId: [{}]",
                    KafkaTopics.PAYMENT_REQUEST_CREATED,
                    event.getRequestReference(),
                    event.getCorrelationId());

            notificationService.notifyPaymentRequestCreated(event);
            idempotencyService.markEventProcessed(event.getEventId(), event.getEventType(), event.getCorrelationId());
        } catch (Exception e) {
            log.error("Error processing PaymentRequestCreatedEvent | reference: [{}] | correlationId: [{}]",
                    event != null ? event.getRequestReference() : "unknown",
                    event != null ? event.getCorrelationId() : "unknown", e);
            throw e; // Rethrow exception so Spring Kafka retry and DLT mechanisms are triggered
        }
    }
}
