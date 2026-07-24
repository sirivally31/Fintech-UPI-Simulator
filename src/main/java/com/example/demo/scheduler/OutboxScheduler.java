package com.example.demo.scheduler;

import com.example.demo.service.OutboxPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled component executing every 5 seconds to poll pending OutboxEvent records
 * and trigger asynchronous publishing via OutboxPublisherService.
 *
 * <p>Design Principle: Contains zero business logic; delegates execution entirely
 * to OutboxPublisherService.</p>
 */
@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxPublisherService outboxPublisherService;

    public OutboxScheduler(OutboxPublisherService outboxPublisherService) {
        this.outboxPublisherService = outboxPublisherService;
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduleOutboxPublishing() {
        try {
            outboxPublisherService.processPendingOutboxEvents();
        } catch (Exception e) {
            log.error("Unhandled exception during OutboxScheduler execution", e);
        }
    }
}
