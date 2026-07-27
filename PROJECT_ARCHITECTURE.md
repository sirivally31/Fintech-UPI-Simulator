# 🏛 Project Architecture - Enterprise Fintech UPI Simulator

This document provides an in-depth architectural blueprint of the Enterprise Fintech UPI Payment Simulator. It details the design patterns, system layers, messaging pipelines, concurrency safeguards, data persistence schemas, and observability stack powering Modules 1 through 15.

---

## 📖 Table of Contents
1. [Architectural Overview](#1-architectural-overview)
2. [Multi-Tier Layered Architecture](#2-multi-tier-layered-architecture)
3. [Security & Authentication Architecture](#3-security--authentication-architecture)
4. [Event-Driven Architecture & Apache Kafka](#4-event-driven-architecture--apache-kafka)
5. [Transactional Outbox Pattern](#5-transactional-outbox-pattern)
6. [Redis Distributed Infrastructure](#6-redis-distributed-infrastructure)
7. [Distributed Concurrency & Locking](#7-distributed-concurrency--locking)
8. [Background Scheduling Engine](#8-background-scheduling-engine)
9. [Observability, Metrics & Telemetry](#9-observability-metrics--telemetry)
10. [Containerization & Docker Orchestration](#10-containerization--docker-orchestration)

---

## 1. Architectural Overview

The platform is designed around the core principles of high-throughput payment switches: **strict ACID guarantees**, **eventual consistency for downstream processing**, **zero-trust security**, and **fail-safe execution**.

```
+-----------------------------------------------------------------------------------+
|                                 PRESENTATION LAYER                                |
|  AuthController | BankAccountController | TransactionController | SettlementController... |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                                  SECURITY LAYER                                   |
|   JwtAuthenticationFilter  |  SecurityContext  |  UserDetailsService  |  BCrypt    |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                                   SERVICE LAYER                                   |
|  TransactionServiceImpl  |  AutoPayServiceImpl  |  FraudDetectionServiceImpl...   |
+-------------------+----------------------+-------------------+--------------------+
                    |                      |                   |
                    v                      v                   v
+-----------------------+  +-----------------------+  +-----------------------+
|  REDIS INFRASTRUCTURE |  |  PERSISTENCE LAYER    |  |  OUTBOX ENGINE        |
|  Distributed Lock     |  |  Spring Data JPA      |  |  OutboxEvent Repo     |
|  Rate Limiter Cache   |  |  PostgreSQL Database  |  |  OutboxScheduler      |
+-----------------------+  +-----------------------+  +-----------+-----------+
                                                                  |
                                                                  v
                                                      +-----------------------+
                                                      |  APACHE KAFKA BUS     |
                                                      |  21 Domain Topics     |
                                                      +-----------+-----------+
                                                                  |
                                                                  v
                                                      +-----------------------+
                                                      | CONSUMERS / ALERTS    |
                                                      | NotificationConsumer  |
                                                      +-----------------------+
```

---

## 2. Multi-Tier Layered Architecture

The application enforces a strict unidirectional dependency flow across five distinct architectural layers:

```
Controllers (REST API) ──► Services (Business Rules) ──► Domain Entities ──► Repositories (JPA) ──► PostgreSQL
```

### Layer Responsibilities

#### 1. Presentation Layer (Controllers)
- Handles HTTP request mapping, JSON deserialization, query parameters, and header parsing.
- Enforces request body validation using JSR-380 `@Valid` annotations (`@NotNull`, `@Size`, `@Pattern`).
- Returns standardized response wrappers (`ApiResponse<T>`) containing timestamp, HTTP status, message, and data payload.

#### 2. Security Filter Layer
- Intercepts requests prior to controller execution via `JwtAuthenticationFilter`.
- Extracts standard `Authorization: Bearer <token>` headers, validates JWT signature and expiration, and hydrates `SecurityContextHolder`.

#### 3. Service Layer (Business Logic)
- Contains business implementations (`TransactionServiceImpl`, `AutoPayServiceImpl`, `FraudDetectionServiceImpl`, etc.).
- Manages transactional boundaries using Spring `@Transactional`.
- Implements fine-grained authorization, ownership validation, and idempotency checks.

#### 4. Repository Layer (Data Access)
- Uses Spring Data JPA interfaces extending `JpaRepository`.
- Executes optimized JPQL queries, optimistic locking via `@Version`, and transactional outbox writes.

#### 5. Persistence Layer (PostgreSQL & Redis)
- **PostgreSQL 15:** Primary relational ACID database storing user state, bank accounts, transactions, mandates, settlements, and outbox logs.
- **Redis 7:** In-memory caching, rate-limiting windows, OTP expiry, and distributed lock coordinator.

---

## 3. Security & Authentication Architecture

Security is engineered around **Stateless JWT Token Authentication**, **Role-Based Access Control (RBAC)**, and **Encrypted PIN Storage**.

```
[ Client Request ] ──► [ JwtAuthenticationFilter ] ──► Valid? ──YES──► [ Security Context ] ──► [ Controller ]
                                   │
                                   NO
                                   ▼
                        [ 401 Unauthorized Response ]
```

### Core Security Controls
1. **Stateless JWT Tokens:** 
   - Signed using HMAC-SHA256 with server secrets.
   - Tokens carry claims including `username`, `userId`, `roles`, issuance time, and expiration time (default: 24 hours).
2. **Password & PIN Hashing:**
   - User account passwords and 4-to-6 digit UPI PINs are hashed using **BCrypt** with a configurable strength factor (salt generation per record).
3. **IDOR & Ownership Validation:**
   - Service calls explicitly cross-check the authenticated `userId` against the resource owner (e.g., verifying a VPA or bank account belongs to the caller before executing a debit).
4. **Spring Security Configuration (`SecurityConfig`):**
   - Configures public endpoints (`/api/v1/auth/**`, `/actuator/**`, `/swagger-ui/**`).
   - Enforces `ROLE_ADMIN` authority on administrative `/api/v1/admin/**` and `/api/v1/settlements/**` routes.

---

## 4. Event-Driven Architecture & Apache Kafka

The application uses an event-driven model to publish domain events whenever state changes occur, ensuring asynchronous processing and system decoupling.

### Complete Topic Inventory

```
+--------------------------------------------------------------------------------+
|                             KAFKA TOPIC REGISTRY                               |
+----------------------------------+---------------------------------------------+
| Category                         | Topic Name                                  |
+----------------------------------+---------------------------------------------+
| Transactions                     | upi.transaction.completed                   |
| Payment Requests                 | upi.payment-request.created                 |
|                                  | upi.payment-request.accepted                |
|                                  | upi.payment-request.rejected                |
|                                  | upi.payment-request.cancelled               |
| QR Payments                      | upi.qr.created                              |
|                                  | upi.qr.payment.success                      |
| Beneficiaries                    | upi.beneficiary.added                       |
|                                  | upi.beneficiary.updated                     |
|                                  | upi.beneficiary.deleted                     |
| AutoPay Mandates                 | upi.autopay.created                         |
|                                  | upi.autopay.executed                        |
|                                  | upi.autopay.failed                          |
|                                  | upi.autopay.cancelled                       |
| Fraud & Risk                     | upi.fraud.detected                          |
|                                  | upi.fraud.blocked                           |
|                                  | upi.fraud.high-risk                         |
| Settlements                      | upi.settlement.completed                    |
|                                  | upi.settlement.failed                       |
|                                  | upi.settlement.reconciled                   |
|                                  | upi.settlement.reversed                     |
+----------------------------------+---------------------------------------------+
```

### Event Producers & Consumers
- **`KafkaEventPublisher`**: Dispatches event messages asynchronously. Handles message key partitioning (partitioning by `transactionReference` or `payerUpiId` to guarantee ordered message processing).
- **`NotificationEventConsumer`**: Subscribes to events across transaction, collect, mandate, and fraud topics. Constructs user notifications and persists them to the inbox while triggering async delivery channels.

---

## 5. Transactional Outbox Pattern

To eliminate the **Dual Write Problem** (where a database commit succeeds but a network issue prevents sending a Kafka event), the system utilizes the Transactional Outbox Pattern.

```
+-----------------------------------------------------------------------------------+
|                              LOCAL DB TRANSACTION                                 |
|                                                                                   |
|  1. UPDATE BankAccount SET balance = balance - amount WHERE id = :senderId       |
|  2. UPDATE BankAccount SET balance = balance + amount WHERE id = :receiverId     |
|  3. INSERT INTO Transaction (txn_ref, amount, status...) VALUES (...)             |
|  4. INSERT INTO OutboxEvent (aggregate_type, payload, status='PENDING'...)        |
|                                                                                   |
|  ===> COMMIT TRANSACTION (Atomically guarantees data + event write)               |
+-----------------------------------------+-----------------------------------------+
                                          |
                                    (Polling Loop)
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                                 OUTBOX SCHEDULER                                  |
|  1. SELECT * FROM OutboxEvent WHERE status = 'PENDING' LIMIT 100                  |
|  2. Publish to Kafka Topic via KafkaTemplate                                      |
|  3. UPDATE OutboxEvent SET status = 'PUBLISHED' WHERE id = :eventId               |
+-----------------------------------------------------------------------------------+
```

### Outbox Advantages
- **No Lost Events:** Events are persisted in the primary database as part of the business transaction.
- **At-Least-Once Delivery:** Retries automatically kick in if Kafka broker connectivity is interrupted.
- **Idempotent Consumers:** Consumers log processed `eventId` values into the `ProcessedEvent` table to drop duplicates gracefully.

---

## 6. Redis Distributed Infrastructure

Redis is integrated as a high-speed in-memory store servicing eight primary modules:

```
+-----------------------------------------------------------------------------------+
|                           REDIS IN-MEMORY MEMORY MAP                              |
+-----------------------+-----------------------+-----------------------------------+
| Subsystem             | Key Format Pattern    | Expiration / Purpose              |
+-----------------------+-----------------------+-----------------------------------+
| Caching               | cache:vpa:{vpa}       | 15 mins TTL (Fast VPA resolution) |
| Rate Limiter          | rate:{ip/user}:{uri}  | 1 min window (DDoS protection)    |
| OTP Storage           | otp:{phone/email}     | 5 mins TTL (Auth verification)    |
| Distributed Locks     | lock:account:{accNo}  | 10 sec lock (Concurrency protection)|
| Fraud Counters        | fraud:velocity:{vpa}  | 1 hour window (Velocity metrics)  |
| Notification Badges   | notif:unread:{userId} | Invalidation on read (Count cache)|
| Merchant & QR Cache   | merchant:qr:{token}   | Dynamic expiry (QR validation)    |
| Beneficiary Payees    | ben:list:{userId}     | 30 mins TTL (Payee directory)     |
+-----------------------+-----------------------+-----------------------------------+
```

---

## 7. Distributed Concurrency & Locking

Payment systems must guarantee that concurrent financial requests never result in **race conditions**, **negative balances**, or **double spending**.

```
[ Thread A: Debit Request ] \
                             ===> [ DistributedLockService.acquireLock("lock:account:1001") ]
[ Thread B: Debit Request ] /
                                                  │
                      ┌───────────────────────────┴───────────────────────────┐
                      ▼                                                       ▼
            [ Thread A Acquired Lock ]                              [ Thread B Blocked ]
                      │                                                       │
         Executes Balance Validation & Debit                                 Waits / Times out
                      │                                                       │
            [ Releases Lock ] ────────────────────────────────────────────────┘
```

### Concurrency Mechanisms
1. **Redis Distributed Locks:** `DistributedLockService` executes atomic key acquisition with lock timeouts to prevent deadlocks.
2. **JPA Optimistic Locking:** Core entities (`BankAccount`, `UpiId`) feature `@Version` fields. Concurrent updates trigger `OptimisticLockException`, which is caught and retried or cleanly rejected.

---

## 8. Background Scheduling Engine

Background worker threads execute recurring system tasks asynchronously:

- **AutoPay Mandate Scheduler (`AutoPayScheduler`):** Runs every 60 seconds. Queries active mandates due for execution, acquires distributed locks for source accounts, executes P2P transfers, and logs results.
- **Merchant Settlement Scheduler (`SettlementScheduler`):** Daily/Hourly cron task aggregating unsettled merchant transactions into batches, deducting platform fees, computing net payouts, and recording settlement entries.
- **Outbox Publisher (`OutboxScheduler`):** Polling thread executing every 500ms to stream unpublished outbox events to Kafka.
- **System Maintenance Jobs:** Periodic purging of expired dynamic QR tokens and stale processed message logs.

---

## 9. Observability, Metrics & Telemetry

Enterprise operational visibility is provided via Spring Boot Actuator and Prometheus Micrometer integrations:

- **Business Metrics:** Custom meters tracking transaction success/failure counts, payment volume in INR, active mandates count, fraud detection alerts, and rate limit blocks.
- **Infrastructure Metrics:** JVM heap usage, GC pause duration, HikariCP database connection pool metrics, Redis cache hit/miss ratio, and Kafka record send latencies.
- **Prometheus Endpoint:** Promoted at `/actuator/prometheus`.
- **Grafana Provisioning:** Automated Grafana dashboard provisioned in `monitoring/grafana/dashboards`.

---

## 10. Containerization & Docker Orchestration

The application stack is fully containerized using Multi-Stage Docker builds and orchestrated via `docker-compose.yml`:

```
+-----------------------------------------------------------------------------------+
|                            DOCKER COMPOSE TOPOLOGY                                |
+-----------------------------------------------------------------------------------+
|  Service Name    | Image / Build                 | Internal Ports | External Ports|
+------------------+-------------------------------+----------------+---------------+
| app              | Spring Boot (Java 17 Multi)   | 8080           | 8080          |
| postgres         | postgres:15-alpine            | 5432           | 5432          |
| redis            | redis:7-alpine                | 6379           | 6379          |
| zookeeper        | confluentinc/cp-zookeeper     | 2181           | 2181          |
| kafka            | confluentinc/cp-kafka         | 9092           | 9092          |
| kafka-ui         | provectuslabs/kafka-ui        | 8080           | 8085          |
| prometheus       | prom/prometheus:latest        | 9090           | 9090          |
| grafana          | grafana/grafana:latest        | 3000           | 3000          |
+------------------+-------------------------------+----------------+---------------+
```

---
*For API reference details, consult [API_DOCUMENTATION.md](file:///e:/UPI%20Simulator/API_DOCUMENTATION.md). For data schemas, consult [SYSTEM_DESIGN.md](file:///e:/UPI%20Simulator/SYSTEM_DESIGN.md).*
