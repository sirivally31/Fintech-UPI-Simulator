package com.example.demo.service.impl;

import com.example.demo.service.ReplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Implementation of ReplayService for republishing messages from Dead Letter Topics (DLT)
 * back to original destination topics.
 */
@Service
public class ReplayServiceImpl implements ReplayService {

    private static final Logger log = LoggerFactory.getLogger(ReplayServiceImpl.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReplayServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void replayEventFromDlt(String dltTopic, String key, Object payload) {
        if (dltTopic == null || !dltTopic.endsWith(".DLT")) {
            log.error("Cannot replay event: invalid DLT topic name [{}]", dltTopic);
            return;
        }

        String targetTopic = dltTopic.substring(0, dltTopic.length() - 4); // Strip '.DLT'
        log.info("Replaying event from DLT topic [{}] to target topic [{}] | key: [{}]", dltTopic, targetTopic, key);

        kafkaTemplate.send(targetTopic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully replayed event to topic [{}] | partition: [{}] | offset: [{}]",
                                targetTopic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to replay event to topic [{}]", targetTopic, ex);
                    }
                });
    }

    @Override
    public void replayAllFailedEvents(String dltTopic) {
        log.info("Initiated bulk event replay for DLT topic [{}]", dltTopic);
        // Bulk replay mechanism logic for consuming all records from DLT topic and republishing
    }
}
