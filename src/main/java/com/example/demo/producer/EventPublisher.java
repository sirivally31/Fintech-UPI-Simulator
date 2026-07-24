package com.example.demo.producer;

import com.example.demo.events.*;

/**
 * EventPublisher contract defining domain event publishing methods for Kafka messaging.
 * Implementation will be created in a subsequent phase.
 */
public interface EventPublisher {

    void publishTransactionCompleted(TransactionCompletedEvent event);

    void publishPaymentRequestCreated(PaymentRequestCreatedEvent event);

    void publishPaymentRequestAccepted(PaymentRequestAcceptedEvent event);

    void publishPaymentRequestRejected(PaymentRequestRejectedEvent event);

    void publishPaymentRequestCancelled(PaymentRequestCancelledEvent event);
}
