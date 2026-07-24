# Transactional Outbox Pattern & Event-Driven Architecture

## 1. Title & Executive Summary
**Guaranteed Event Delivery, Transactional Outbox Pattern, & Kafka Event Bus**

This document describes the architectural implementation of the Transactional Outbox Pattern and event-driven messaging topology in the UPI Payment Simulator. It details how the platform avoids dual-write inconsistencies, guarantees At-Least-Once event publishing, and maintains consistency across distributed consumers.

---

## 2. Why the Feature Exists
In microservice and fintech architectures, a single business operation (such as completing a $500 money transfer) often requires two distinct state updates:
1. Updating local application state in PostgreSQL (`UPDATE bank_accounts`, `INSERT INTO transactions`).
2. Publishing an event notification to Apache Kafka (`transaction-completed-events`).

### The Dual-Write Problem
Executing these two actions separately in a standard Spring service method creates fatal consistency flaws:
- **Scenario A (DB Succeeded, Kafka Failed)**: If the DB transaction commits successfully but the network drops before publishing to Kafka, downstream audit services, fraud detectors, and notification engines never learn about the transaction.
- **Scenario B (Kafka Succeeded, DB Failed)**: If the Kafka message is published first, but the DB transaction subsequently rolls back due to a constraint violation, downstream services act on phantom money that was never actually transferred.

---

## 3. Enterprise Architecture (Transactional Outbox)

The **Transactional Outbox Pattern** solves dual-write inconsistencies by converting remote messaging calls into a local database write within the exact same ACID database transaction:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   SPRING @TRANSACTIONAL BOUNDARY                       │
│                                                                        │
│  1. Debit Sender Balance       ──▶ UPDATE bank_accounts               │
│  2. Credit Receiver Balance    ──▶ UPDATE bank_accounts               │
│  3. Save Transaction Ledger    ──▶ INSERT INTO transactions           │
│  4. Save Outbox Event Payload  ──▶ INSERT INTO outbox_events(PENDING) │
│                                                                        │
│                    [ATOMIC DATABASE COMMIT]                            │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     BACKGROUND SCHEDULER & BUS                         │
│                                                                        │
│  5. OutboxScheduler (Cron Poll) ──▶ SELECT * FROM outbox_events        │
│                                    WHERE status = 'PENDING'            │
│  6. OutboxPublisherServiceImpl ──▶ KafkaTemplate.send(...)             │
│  7. Kafka Broker ACK            ──▶ UPDATE outbox_events               │
│                                    SET status = 'PROCESSED'            │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. How Our Implementation Works

### 1. Outbox Event Persistence (`OutboxServiceImpl`)
When `TransactionServiceImpl.sendMoney()` completes financial logic, it constructs a domain event (`TransactionCompletedEvent`) and calls:
```java
outboxService.saveOutboxEvent(
    event.getEventId(),
    "TRANSACTION",
    transaction.getId(),
    "TRANSACTION_COMPLETED",
    correlationId,
    event
);
```
`OutboxServiceImpl` serializes the Java event object into JSON and inserts an `OutboxEvent` entity into the `outbox_events` table with status `PENDING`. Because this insert occurs within the `@Transactional` method, it shares the exact same database connection and transaction context as the balance updates.

### 2. Polling Scheduler (`OutboxScheduler`)
An automated Spring background task (`OutboxScheduler`) fires every 5 seconds (`@Scheduled(fixedDelay = 5000)`):
- Queries up to 50 pending records: `outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING", PageRequest.of(0, 50))`.
- For each pending event, it passes the entity to `OutboxPublisherService`.

### 3. Kafka Publishing & Acknowledgment (`OutboxPublisherServiceImpl`)
- Maps the `aggregateType` or `eventType` to the corresponding target Kafka topic (`transaction-completed-events`, `payment-request-created-events`, etc.).
- Invokes `kafkaTemplate.send(topic, eventId.toString(), payloadJson)`.
- Upon receiving a synchronous or asynchronous success callback from Kafka, `OutboxPublisherServiceImpl` updates `outboxEvent.setStatus("PROCESSED")` and sets `outboxEvent.setProcessedAt(LocalDateTime.now())`.

---

## 5. Event Data Models

Every outbox event carries standard enterprise headers:
- `eventId` (`UUID`): Unique event tracking identifier for consumer deduplication.
- `eventType` (`String`): Schema descriptor (e.g. `TransactionCompleted`, `PaymentRequestAccepted`).
- `correlationId` (`String`): Distributed tracing correlation ID across API and Kafka logs.
- `eventTime` (`LocalDateTime`): Event creation timestamp.
- `payload` (`JSONB / Text`): Structured domain payload containing amounts, accounts, VPAs, and status.

---

## 6. Database Interaction & Schemas

### `outbox_events` Table Definition
```sql
CREATE TABLE outbox_events (
    event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP
);
```

---

## 7. Spring Boot Components Involved

- `com.example.demo.entity.OutboxEvent`: Entity representation of outbox table.
- `com.example.demo.service.OutboxService` & `OutboxServiceImpl`: In-transaction event recording helper.
- `com.example.demo.scheduler.OutboxScheduler`: Scheduled polling component (`@Scheduled`).
- `com.example.demo.service.impl.OutboxPublisherServiceImpl`: Message publisher interacting with `KafkaTemplate`.

---

## 8. Security & Resilience Considerations

- **At-Least-Once Delivery Safeguards**: If the publisher application crashes before marking the outbox event as `PROCESSED`, the scheduler will re-read the `PENDING` record upon reboot and re-publish to Kafka. Consumers use `eventId` deduplication to ensure idempotency.
- **Payload Tampering Protection**: Outbox event payloads are immutable once written.

---

## 9. Future Improvements

- **PostgreSQL Change Data Capture (CDC) via Debezium**: Replacing scheduled polling with native PostgreSQL WAL stream listening for zero-latency outbox publishing.
- **Partition Key Strategy**: Explicit partitioning by `senderBankAccountId` to enforce strict sequential event ordering per user.
