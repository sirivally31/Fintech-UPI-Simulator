package com.example.demo.service.impl;

import com.example.demo.config.TopicResolver;
import com.example.demo.entity.OutboxEvent;
import com.example.demo.entity.OutboxStatus;
import com.example.demo.repository.OutboxEventRepository;
import com.example.demo.service.OutboxPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.metrics.BusinessMetricsService;

/**
 * Implementation of OutboxPublisherService for processing pending outbox events,
 * publishing them to Kafka, and updating outbox status and retry telemetry.
 */
@Service
public class OutboxPublisherServiceImpl implements OutboxPublisherService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherServiceImpl.class);
    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final TopicResolver topicResolver;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BusinessMetricsService businessMetricsService;

    public OutboxPublisherServiceImpl(OutboxEventRepository outboxEventRepository,
                                      TopicResolver topicResolver,
                                      KafkaTemplate<String, Object> kafkaTemplate,
                                      BusinessMetricsService businessMetricsService) {
        this.outboxEventRepository = outboxEventRepository;
        this.topicResolver = topicResolver;
        this.kafkaTemplate = kafkaTemplate;
        this.businessMetricsService = businessMetricsService;
    }

    @Override
    @Transactional
    public void processPendingOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found [{}] pending outbox events to process", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            publishSingleOutboxEvent(event);
        }
    }

    private void publishSingleOutboxEvent(OutboxEvent event) {
        try {
            String topic = topicResolver.resolveTopic(event.getEventType());
            String key = event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId().toString();

            log.info("Outbox Publishing Attempt | eventId: [{}] | aggregateId: [{}] | aggregateType: [{}] | " +
                            "eventType: [{}] | status: [{}] | retryCount: [{}] | correlationId: [{}] | timestamp: [{}]",
                    event.getEventId(),
                    event.getAggregateId(),
                    event.getAggregateType(),
                    event.getEventType(),
                    event.getStatus(),
                    event.getRetryCount(),
                    event.getCorrelationId(),
                    LocalDateTime.now());

            kafkaTemplate.send(topic, key, event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            handleSuccess(event);
                        } else {
                            handleFailure(event, ex);
                        }
                    });
        } catch (Exception ex) {
            handleFailure(event, ex);
        }
    }

    private void handleSuccess(OutboxEvent event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
        businessMetricsService.recordOutboxPublished(event.getEventType());

        log.info("Outbox Publishing SUCCESS | eventId: [{}] | aggregateId: [{}] | aggregateType: [{}] | " +
                        "eventType: [{}] | status: [{}] | retryCount: [{}] | correlationId: [{}] | publishedAt: [{}]",
                event.getEventId(),
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                event.getStatus(),
                event.getRetryCount(),
                event.getCorrelationId(),
                event.getPublishedAt());
    }

    private void handleFailure(OutboxEvent event, Throwable ex) {
        int newRetryCount = event.getRetryCount() + 1;
        event.setRetryCount(newRetryCount);
        event.setLastError(ex.getMessage());

        if (newRetryCount >= MAX_RETRIES) {
            event.setStatus(OutboxStatus.FAILED);
        }

        outboxEventRepository.save(event);
        businessMetricsService.recordOutboxFailed(event.getEventType(), ex.getMessage());

        log.error("Outbox Publishing ERROR | eventId: [{}] | aggregateId: [{}] | aggregateType: [{}] | " +
                        "eventType: [{}] | status: [{}] | retryCount: [{}]/[{}] | correlationId: [{}]",
                event.getEventId(),
                event.getAggregateId(),
                event.getAggregateType(),
                event.getEventType(),
                event.getStatus(),
                newRetryCount,
                MAX_RETRIES,
                event.getCorrelationId(),
                ex);
    }
}
