# Apache Kafka Topology & Resilience Architecture

## 1. Title & Executive Summary
**Kafka Event Bus, Idempotent Consumers, Retry Topics, & Dead Letter Topics (DLT)**

This document details the event streaming architecture powered by Apache Kafka in the UPI Simulator. It explains topic structures, producer configurations, consumer groups, retry backoff mechanisms, Dead Letter Topic (DLT) isolation, and idempotency tracking.

---

## 2. Why the Feature Exists
In distributed microservices, downstream operations (such as sending push notifications, auditing ledgers, or triggering merchant webhooks) must be handled asynchronously:
- **Decoupled Architecture**: Financial payment execution must not wait for push notification services or third-party webhooks to complete.
- **Consumer Fault Tolerance**: Transient network glitches or temporary database outages in downstream consumers must not cause message loss.
- **Poison-Pill Isolation**: Malformed or unprocessable messages ("poison pills") must be isolated to a Dead Letter Topic without halting the processing of healthy messages in the main topic queue.

---

## 3. Enterprise Architecture (Kafka Pipelines)

```
┌─────────────────────────┐
│ OutboxPublisherService  │
└────────────┬────────────┘
             │ (Publish Payload)
             ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   MAIN KAFKA TOPIC                                     │
│     transaction-completed-events (Partitioned by Key)                │
└────────────┬───────────────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────┐     Failed?      ┌──────────────────────────┐
│ TransactionConsumer     ├─────────────────►│ RETRY TOPIC (.RETRY)     │
└────────────┬────────────┘  (Retry Backoff) └────────────┬─────────────┘
             │                                            │ Retries Exhausted
             ▼ (Success)                                  ▼
┌─────────────────────────┐                  ┌──────────────────────────┐
│ Insert processed_events │                  │ DEAD LETTER TOPIC (.DLT) │
└─────────────────────────┘                  └────────────┬─────────────┘
                                                          │
                                                          ▼
                                                 [ReplayService UI]
```

---

## 4. How Our Implementation Works

### 1. Producer Configuration (`KafkaConfig` & `OutboxPublisherServiceImpl`)
- Uses `KafkaTemplate<String, Object>`.
- **Serializers**: `StringSerializer` for record keys, `JsonSerializer` for payloads.
- **Headers**: Injects type information and correlation IDs into record headers (`spring.json.trusted.packages=com.example.demo.events`).

### 2. Idempotent Consumer (`TransactionEventConsumer`)
- Listens to topic `transaction-completed-events` within consumer group `upi-simulator-group`.
- Extracts `eventId` from the `TransactionCompletedEvent` payload.
- **Idempotency Guard**:
  - Queries `processedEventRepository.existsById(eventId)`.
  - If `true`, logs warning and skips processing (prevents double-execution during network retries).
  - If `false`, executes processing logic and inserts a new `ProcessedEvent(eventId, eventType, LocalDateTime.now())` entity into PostgreSQL.

### 3. Automatic Retries & Dead Letter Topics (`@RetryableTopic`)
- Annotates consumer methods with `@RetryableTopic`:
  - **Attempts**: 3 attempts.
  - **Backoff**: Exponential backoff (delay: 1000ms, multiplier: 2.0).
  - **DltStrategy**: `FAIL_ON_ERROR` (routes to `.DLT` topic upon final failure).
- If processing fails 3 times, Spring Kafka publishes the record to `transaction-completed-events-dlt`.

### 4. Dead Letter Replay (`ReplayService`)
- Provides administration functionality to inspect messages in `.DLT` topics.
- `ReplayService.replayEvent(dltTopic, eventId)` re-publishes the failed event back to the main topic once underlying bugs or outages are resolved.

---

## 5. Kafka Topic Specifications

| Topic Name | Partitions | Replication | Purpose |
| :--- | :--- | :--- | :--- |
| `transaction-completed-events` | 3 | 1 (Local) / 3 (Prod) | Published upon money transfer completion |
| `payment-request-created-events` | 3 | 1 (Local) / 3 (Prod) | Published when collect request is created |
| `payment-request-accepted-events` | 3 | 1 (Local) / 3 (Prod) | Published when collect request is approved |
| `payment-request-rejected-events` | 3 | 1 (Local) / 3 (Prod) | Published when collect request is declined |
| `transaction-completed-events-dlt` | 1 | 1 | Dead Letter Topic for failed transactions |

---

## 6. Database Interaction (Processed Events Schema)

```sql
CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);
```

---

## 7. Spring Boot Components Involved

- `org.springframework.kafka.core.KafkaTemplate`: Message producer abstraction.
- `org.springframework.kafka.annotation.KafkaListener`: Consumer method listener stereotype.
- `org.springframework.kafka.annotation.RetryableTopic`: Declarative retry and DLT routing configuration.
- `com.example.demo.consumer.TransactionEventConsumer`: Main financial event consumer.
- `com.example.demo.service.ReplayService` & `ReplayServiceImpl`: DLT event replay manager.

---

## 8. Security & Resilience Considerations

- **Consumer Group Isolation**: Multiple instances of the application share the same `groupId` (`upi-simulator-group`), enabling automatic partition rebalancing.
- **Deserialization Security**: Explicit trusted packages rule prevents remote code execution vulnerabilities during JSON deserialization.

---

## 9. Future Improvements

- **Schema Registry Integration**: Adopting Apache Avro and Confluent Schema Registry for strict event schema evolution.
- **Kafka Transactions (`read-committed`)**: Enabling Kafka transactional producers to guarantee atomic writes across multiple topics.
