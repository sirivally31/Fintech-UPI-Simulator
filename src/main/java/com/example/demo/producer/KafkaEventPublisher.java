package com.example.demo.producer;

import com.example.demo.config.KafkaTopics;
import com.example.demo.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import com.example.demo.metrics.BusinessMetricsService;

/**
 * Spring Component implementing EventPublisher for dispatching domain events
 * to Apache Kafka topics with asynchronous callbacks and structured SLF4J logging.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BusinessMetricsService businessMetricsService;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               BusinessMetricsService businessMetricsService) {
        this.kafkaTemplate = kafkaTemplate;
        this.businessMetricsService = businessMetricsService;
    }

    @Override
    public void publishTransactionCompleted(TransactionCompletedEvent event) {
        sendEvent(KafkaTopics.TRANSACTION_COMPLETED, event.getTransactionReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishPaymentRequestCreated(PaymentRequestCreatedEvent event) {
        sendEvent(KafkaTopics.PAYMENT_REQUEST_CREATED, event.getRequestReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishPaymentRequestAccepted(PaymentRequestAcceptedEvent event) {
        sendEvent(KafkaTopics.PAYMENT_REQUEST_ACCEPTED, event.getRequestReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishPaymentRequestRejected(PaymentRequestRejectedEvent event) {
        sendEvent(KafkaTopics.PAYMENT_REQUEST_REJECTED, event.getRequestReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishPaymentRequestCancelled(PaymentRequestCancelledEvent event) {
        sendEvent(KafkaTopics.PAYMENT_REQUEST_CANCELLED, event.getRequestReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishQrCreated(QrCreatedEvent event) {
        sendEvent(KafkaTopics.QR_CREATED, event.getQrToken(), event.getCorrelationId(), event);
    }

    @Override
    public void publishQrPaymentSuccess(QrPaymentSuccessEvent event) {
        sendEvent(KafkaTopics.QR_PAYMENT_SUCCESS, event.getTransactionReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishBeneficiaryAdded(BeneficiaryAddedEvent event) {
        sendEvent(KafkaTopics.BENEFICIARY_ADDED, event.getBeneficiaryId().toString(), event.getCorrelationId(), event);
    }

    @Override
    public void publishBeneficiaryUpdated(BeneficiaryUpdatedEvent event) {
        sendEvent(KafkaTopics.BENEFICIARY_UPDATED, event.getBeneficiaryId().toString(), event.getCorrelationId(), event);
    }

    @Override
    public void publishBeneficiaryDeleted(BeneficiaryDeletedEvent event) {
        sendEvent(KafkaTopics.BENEFICIARY_DELETED, event.getBeneficiaryId().toString(), event.getCorrelationId(), event);
    }

    @Override
    public void publishAutoPayCreated(AutoPayCreatedEvent event) {
        sendEvent(KafkaTopics.AUTOPAY_CREATED, event.getMandateReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishAutoPayExecuted(AutoPayExecutedEvent event) {
        sendEvent(KafkaTopics.AUTOPAY_EXECUTED, event.getMandateReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishAutoPayFailed(AutoPayFailedEvent event) {
        sendEvent(KafkaTopics.AUTOPAY_FAILED, event.getMandateReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishAutoPayCancelled(AutoPayCancelledEvent event) {
        sendEvent(KafkaTopics.AUTOPAY_CANCELLED, event.getMandateReference(), event.getCorrelationId(), event);
    }

    @Override
    public void publishFraudDetected(FraudDetectedEvent event) {
        sendEvent(KafkaTopics.FRAUD_DETECTED, event.getPayerUpiId(), event.getCorrelationId(), event);
    }

    @Override
    public void publishFraudBlocked(FraudBlockedEvent event) {
        sendEvent(KafkaTopics.FRAUD_BLOCKED, event.getPayerUpiId(), event.getCorrelationId(), event);
    }

    @Override
    public void publishHighRiskTransaction(HighRiskTransactionEvent event) {
        sendEvent(KafkaTopics.HIGH_RISK_TRANSACTION, event.getPayerUpiId(), event.getCorrelationId(), event);
    }

    /**
     * Private helper method to handle event publishing, asynchronous callbacks, and structured logging.
     *
     * @param topic         Target Kafka topic
     * @param key           Message key (business reference such as transaction or request reference)
     * @param correlationId System correlation ID for distributed tracing
     * @param event         Event payload
     */
    private void sendEvent(String topic, String key, String correlationId, Object event) {
        String eventType = event.getClass().getSimpleName();
        log.info("Publishing event [{}] to topic [{}] | reference: [{}] | correlationId: [{}]",
                eventType, topic, key, correlationId);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published event [{}] to topic [{}] | partition: [{}] | offset: [{}] | reference: [{}] | correlationId: [{}]",
                        eventType,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key,
                        correlationId);
                businessMetricsService.recordKafkaEventSent(topic);
            } else {
                log.error("Failed to publish event [{}] to topic [{}] | reference: [{}] | correlationId: [{}]",
                        eventType, topic, key, correlationId, ex);
                businessMetricsService.recordKafkaEventFailed(topic, ex.getMessage());
            }
        });
    }
}
