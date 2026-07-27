# 🚀 Enterprise Fintech UPI Payment Simulator

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/sirivally31/Fintech-UPI-Simulator)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red.svg)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An enterprise-grade, high-throughput backend infrastructure simulating a real-world Unified Payments Interface (UPI) platform. Inspired by leading payment service providers (PSPs) and NPCI core banking architectures—including **PhonePe**, **Google Pay**, **Paytm**, **BHIM**, and core banking switches.

The platform provides end-to-end payment processing, automated mandates, dynamic merchant settlement, automated fraud risk scoring, transactional outbox event delivery, distributed locking, and real-time operational observability.

---

## 📖 Table of Contents
- [Project Overview](#-project-overview)
- [Implemented Modules (Modules 1–15)](#-implemented-modules-modules-115)
- [Current System Architecture](#-current-system-architecture)
- [Database & Data Models](#-database--data-models)
- [API Summary](#-api-summary)
- [Event-Driven Architecture & Kafka](#-event-driven-architecture--kafka)
- [Redis Distributed Infrastructure](#-redis-distributed-infrastructure)
- [Background Schedulers](#-background-schedulers)
- [Project Directory Structure](#-project-directory-structure)
- [Feature Matrix](#-feature-matrix)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [Observability & Monitoring](#-observability--monitoring)
- [Known Issues](#-known-issues)
- [Next Phase Development](#-next-phase-development)
- [Getting Started & Local Setup](#-getting-started--local-setup)
- [Author & Acknowledgments](#-author--acknowledgments)

---

## 📖 Project Overview

The **Enterprise Fintech UPI Simulator** is built to model the exact operational workflows, resilience patterns, and transaction guarantees found in tier-1 financial switches. 

### Key Highlights
- **High-Throughput ACID Transactions:** Atomic debit/credit transfers across linked bank accounts with optimistic locking.
- **Enterprise Security:** JWT-based stateless authentication, BCrypt hashing, fine-grained RBAC, IDOR protection, and encrypted 6-digit UPI PIN storage.
- **Event-Driven Resilience:** Guaranteed event delivery using the **Transactional Outbox Pattern** with Apache Kafka, eliminating the Dual Write Problem.
- **Distributed Concurrency:** Redis-backed distributed locks to prevent double-spending and race conditions during concurrent payments and pin modifications.
- **Merchant Ecosystem:** QR code generation (static & dynamic), merchant registration, instant webhooks, and automated T+1/hourly settlement batching.
- **Automated Mandates (AutoPay):** Recurring scheduled subscription payments with automated retry mechanisms and user-configured frequency/limits.
- **Real-Time Fraud Engine:** Rule-based risk scoring checking transaction velocity, amount thresholds, and blacklisted accounts.
- **Full Operational Visibility:** Prometheus metrics export and Grafana dashboards for JVM, HTTP latency, transaction throughput, and Kafka/Redis performance.

---

## 📦 Implemented Modules (Modules 1–15)

All 15 core backend modules have been fully designed, implemented, integrated, and verified with unit and integration tests:

| Module | Name | Key Functionalities |
| :--- | :--- | :--- |
| **Module 1** | **Authentication & JWT** | User registration, JWT token generation, refresh tokens, BCrypt password hashing, Spring Security filter chains. |
| **Module 2** | **Bank Account Management** | Link/unlink core bank accounts, balance fetching, default account selection, account status validation (ACTIVE/SUSPENDED). |
| **Module 3** | **UPI ID Management** | Create custom Virtual Payment Addresses (`username@upi`), alias mapping, primary status toggles, availability checks. |
| **Module 4** | **UPI PIN Management** | Encrypted set/change/reset 4-to-6 digit UPI PIN, failed attempts lock, BCrypt verification, security salt. |
| **Module 5** | **Money Transfer** | P2P transfer execution, atomic balance debit/credit, transaction history, idempotency handling, transaction receipt generation. |
| **Module 6** | **Payment Requests** | Collect request creation, accept/reject/cancel workflows, expiry cron handling, real-time push events. |
| **Module 7** | **Enterprise Infrastructure** | Multi-container Docker Compose setup, PostgreSQL schemas, Redis cache cluster, Kafka event bus, Outbox publisher, Actuator. |
| **Module 8** | **Merchant QR Payment System** | Merchant onboarding, category codes (MCC), static & dynamic QR code generation, QR payload signing & verification. |
| **Module 9** | **QR Payment Execution** | Merchant QR scanning, payment execution against static/dynamic QR codes, instant merchant credit & webhook trigger. |
| **Module 10** | **Beneficiary Management** | Manage trusted payees/contacts, nickname assignment, transfer caps for unverified beneficiaries, quick-pay aliases. |
| **Module 11** | **AutoPay & Scheduled Payments** | Mandate creation, authorization, recurring schedule execution (DAILY, WEEKLY, MONTHLY), mandate cancellation, execution logs. |
| **Module 12** | **Fraud Detection & Risk Engine** | Rule-based fraud evaluation, high-velocity detection, high-value alerts, suspicious account blacklisting, risk scoring. |
| **Module 13** | **Notification Service** | Async event consumer for SMS/Email/Push alerts, user notification inbox, read/unread state management. |
| **Module 14** | **Administrative Dashboard** | System health metrics, transaction volume analytics, user management, merchant approval, fraud rule management. |
| **Module 15** | **Settlement & Reconciliation** | Automated merchant batch settlement engine, fee deduction calculations, reconciliation reports, reversal handling. |

---

## 🏗 Current System Architecture

The architecture strictly adheres to enterprise multi-tier clean design principles, separating controller endpoints, business services, domain logic, data access, and async messaging.

```
                     +----------------------------------+
                     |         Clients / Frontend       |
                     |   (Postman / Swagger / Web UI)   |
                     +-----------------+----------------+
                                       |
                                REST APIs (JWT)
                                       |
                                       v
                     +----------------------------------+
                     |    Presentation Layer            |
                     |    (Spring Controllers)          |
                     +-----------------+----------------+
                                       |
                                       v
                     +----------------------------------+
                     |    Service Layer & Security      |
                     |  (Spring Security / JWT Filter)  |
                     +-----------------+----------------+
                                       |
                                       v
                     +----------------------------------+
                     |    Business Logic & Fraud        |
                     |  (Risk Engine / AutoPay / QR)    |
                     +-------+------------------+-------+
                             |                  |
              +--------------+                  +--------------+
              |                                                |
              v                                                v
    +-------------------+                             +-------------------+
    |  Repository Layer |                             |   Redis Cache &   |
    |   (Spring JPA)    |                             |  Distributed Lock |
    +---------+---------+                             +-------------------+
              |
              v
    +-------------------+           Transactional     +-------------------+
    | PostgreSQL DB     |======= OUTBOX EVENT =======>|  Outbox Scheduler |
    | (ACID Storage)    |           PATTERN           +---------+---------+
    +-------------------+                                       |
                                                                v
                                                      +-------------------+
                                                      |  Apache Kafka     |
                                                      |   Event Bus       |
                                                      +---------+---------+
                                                                |
                                                                v
                                                      +-------------------+
                                                      | Event Consumers & |
                                                      |  Notification     |
                                                      +-------------------+
```

### Infrastructure Components & Integration
- **JWT Authentication:** Stateless security filter parsing authorization headers, validating token signatures, and injecting security principals into Spring Security Context.
- **Redis Cache:** High-performance caching layer for session state, OTP limits, active rate limiting counters, beneficiary lists, merchant details, and QR payload validation.
- **Distributed Locking:** Redis-based locking mechanism (`DistributedLockService`) ensuring thread-safe balance operations, mandate execution, and UPI PIN changes across clustered instances.
- **Kafka Event Bus:** Pub-Sub messaging system streaming domain events across micro-components asynchronously for analytics, notifications, and settlements.
- **Transactional Outbox Pattern:** Outbox table populated inside local DB transactions; background `OutboxPublisher` polls and commits to Kafka with at-least-once delivery guarantee.
- **Prometheus & Grafana:** Spring Actuator metrics scraped by Prometheus server (`localhost:9090`) and rendered on pre-configured Grafana dashboards (`localhost:3000`).
- **Docker Compose:** Complete orchestrator running PostgreSQL 15, Redis 7, Kafka + Zookeeper, Kafka UI, Prometheus, Grafana, and Spring Boot application.

---

## 🗄 Database & Data Models

The relational schema is implemented using PostgreSQL with Spring Data JPA and Hibernate. Key entities and structural relationships include:

```
  +--------------+         +------------------+         +------------------+
  |     User     | 1 --- N |   BankAccount    | 1 --- N |      UpiId       |
  +--------------+         +------------------+         +------------------+
     1      1                     1                             1
     |      |                     |                             |
     N      N                     N                             N
  +----+  +-----+           +-----------+                 +-----------+
  |Ben.|  |Notif|           |Transaction|                 |PaymentReq.|
  +----+  +-----+           +-----------+                 +-----------+
                                  |
                                  | 1
                                  |
                                  | N
                            +-----------+
                            |Settlement |
                            +-----------+
```

### Major Entities Summary
1. **`User`**: Account holder details, credentials hash, phone, email, roles (`ROLE_USER`, `ROLE_ADMIN`), status (`ACTIVE`, `BLOCKED`).
2. **`BankAccount`**: Linked core bank account, account number, IFSC code, bank name, account type (`SAVINGS`, `CURRENT`), balance, optimistic locking version.
3. **`UpiId`**: Virtual Payment Address (`vpa`), linkage to `BankAccount` and `User`, primary VPA status, encrypted UPI PIN hash, account status.
4. **`Transaction`**: Immutable payment ledger record storing `txnReference`, sender/receiver VPA, transaction amount, type (`P2P`, `P2M`, `COLLECT`, `AUTOPAY`), status (`SUCCESS`, `FAILED`, `PENDING`), failure reason, timestamp.
5. **`PaymentRequest`**: Collect request record with `requestReference`, requester VPA, payer VPA, amount, status (`PENDING`, `ACCEPTED`, `REJECTED`, `EXPIRED`), expiry timestamp.
6. **`Merchant`**: Business profile, merchant code, business category (MCC), settlement bank account, API secret keys, status.
7. **`MerchantQr`**: Dynamic/Static QR generator payload, `qrToken`, amount, expiry, active status, link to `Merchant`.
8. **`Beneficiary`**: Saved contact payee linked to `User`, payee name, VPA/Account Number, IFSC, nickname, status.
9. **`AutoPay`**: Standing order mandate record with `mandateReference`, user, source VPA, payee VPA, recurring amount, frequency (`DAILY`, `WEEKLY`, `MONTHLY`), next execution date, max amount limit, status (`ACTIVE`, `PAUSED`, `CANCELLED`).
10. **`FraudRule`**: Automated risk rule definition, `ruleName`, `ruleType`, threshold value, risk score weight, active flag.
11. **`Notification`**: System alert record, user, title, message content, channel (`SMS`, `EMAIL`, `PUSH`), delivery status (`PENDING`, `SENT`, `FAILED`), read flag.
12. **`SettlementBatch`**: Daily/Hourly merchant payout batch, `batchReference`, merchant, total amount, net payout amount, platform fee, transaction count, status (`PENDING`, `COMPLETED`, `FAILED`).
13. **`SettlementEntry`**: Granular link mapping individual transactions to a `SettlementBatch`.
14. **`OutboxEvent`**: Outbox pattern ledger with aggregate type, payload JSON, event type, status (`PENDING`, `PUBLISHED`, `FAILED`), retry count.
15. **`ProcessedEvent`**: Idempotency tracking table storing consumer processed message IDs to prevent duplicate message processing.

---

## 🔌 API Summary

REST endpoints are organized cleanly under modular controllers. All endpoints return standardized JSON payloads wrapped in `ApiResponse<T>`.

### Endpoint Groups Overview
- **Authentication (`/api/v1/auth`)**: Register users, authenticate credentials, obtain JWT Bearer token, refresh sessions.
- **User Management (`/api/v1/users`)**: Fetch current profile, update profile details, change login passwords.
- **Bank Account Management (`/api/v1/accounts`)**: Link core bank account, fetch linked accounts, set primary account, query balance.
- **UPI ID Management (`/api/v1/upi-ids`)**: Register new VPAs, toggle default status, deactivate VPAs, query VPA availability.
- **UPI PIN Security (`/api/v1/upi-pin`)**: Set 4-6 digit UPI PIN, verify PIN, change existing PIN, reset PIN with OTP verification.
- **Money Transfers (`/api/v1/transactions`)**: Execute P2P transfers, query transaction history by reference, list user transactions, generate PDF receipts.
- **Payment Requests (`/api/v1/payment-requests`)**: Create collect request, list pending requests, accept request (requires PIN), reject/cancel request.
- **Merchant System (`/api/v1/merchants`)**: Onboard business merchant, generate static/dynamic QR code, update merchant profile, query merchant analytics.
- **QR Payment Execution (`/api/v1/qr`)**: Parse/validate QR code token, execute merchant payment against scanned QR.
- **Beneficiaries (`/api/v1/beneficiaries`)**: Add payee contact, update beneficiary details, remove payee, list active payees.
- **AutoPay Mandates (`/api/v1/autopay`)**: Create recurring mandate, pause/resume mandate, revoke mandate, query mandate history.
- **Fraud & Risk Management (`/api/v1/fraud`)**: List active fraud rules, update rule thresholds, check VPA risk score, unblock flagged VPAs (Admin).
- **Notifications (`/api/v1/notifications`)**: Retrieve notification inbox, mark notification as read, update channel notification preferences.
- **Admin Dashboard (`/api/v1/admin`)**: View system health, total volume analytics, pending merchant approvals, global transaction logs.
- **Settlement & Reconciliation (`/api/v1/settlements`)**: Trigger manual settlement batching, view merchant settlement history, generate reconciliation reports.

---

## ⚡ Event-Driven Architecture & Kafka

The platform utilizes **Apache Kafka** as its central distributed message broker to achieve asynchronous decoupling and high event throughput.

```
[ Domain Operation ] ──► [ Local DB Transaction ] ──► [ Write Outbox Event ]
                                                             │
                                                     (Async Poller)
                                                             │
                                                             ▼
                                                    [ Outbox Publisher ]
                                                             │
                                                             ▼
                                                    [ Apache Kafka Bus ]
                                                             │
                                        ┌────────────────────┴────────────────────┐
                                        ▼                                         ▼
                           [ Notification Consumer ]                 [ Analytics & Settlement ]
```

### Implemented Kafka Topics
- `upi.transaction.completed` – Dispatched upon successful transaction settlement.
- `upi.payment-request.created` – Dispatched when a user creates a collect request.
- `upi.payment-request.accepted` – Dispatched when a collect request is paid.
- `upi.payment-request.rejected` – Dispatched when a collect request is declined.
- `upi.payment-request.cancelled` – Dispatched when requester revokes collect request.
- `upi.qr.created` – Dispatched when a merchant generates a dynamic QR.
- `upi.qr.payment.success` – Dispatched when a QR-based payment succeeds.
- `upi.beneficiary.added` – Dispatched when a new payee is saved.
- `upi.beneficiary.updated` – Dispatched on payee detail change.
- `upi.beneficiary.deleted` – Dispatched on payee removal.
- `upi.autopay.created` – Dispatched on new mandate setup.
- `upi.autopay.executed` – Dispatched on scheduled auto-debit success.
- `upi.autopay.failed` – Dispatched when recurring execution fails.
- `upi.autopay.cancelled` – Dispatched on mandate revocation.
- `upi.fraud.detected` – Dispatched when risk score exceeds warning threshold.
- `upi.fraud.blocked` – Dispatched when transaction is auto-blocked by risk engine.
- `upi.fraud.high-risk` – Dispatched for administrator review alerts.
- `upi.settlement.completed` – Dispatched when merchant batch settlement completes.
- `upi.settlement.failed` – Dispatched on settlement batch payout failure.
- `upi.settlement.reconciled` – Dispatched when end-of-day bank reconciliation completes.
- `upi.settlement.reversed` – Dispatched when settlement reversal occurs.

### Producers & Consumers
- **`KafkaEventPublisher`**: Spring component converting domain events to JSON and writing to Kafka with async futures and business metrics reporting.
- **`NotificationEventConsumer`**: Asynchronous Kafka listener handling notification topics, implementing idempotency checks via `ProcessedEvent` repository to prevent double execution.

---

## 🔴 Redis Distributed Infrastructure

Redis is integrated as an in-memory datastore serving multiple critical operational roles:

1. **High-Speed Caching (`RedisCacheService`):** Frequently queried data (User profiles, Bank Account metadata, VPA lookups) are cached with configured TTLs to reduce PostgreSQL read load.
2. **Rate Limiting (`RateLimiterService`):** Sliding window rate limiter preventing API abuse and DDoS attacks (e.g., max 10 requests/sec per IP/User).
3. **OTP Cache (`OtpCacheService`):** Temporary, short-lived (5-minute TTL) OTP store for multi-factor authentication and PIN resets.
4. **Distributed Locks (`DistributedLockService`):** Redlock algorithm implementation acquiring key-level locks (e.g., `lock:account:{accountNo}`) ensuring serial execution during concurrent balance transfers.
5. **Fraud Velocity Counters:** Real-time sliding counters tracking transaction counts and cumulative volume per VPA within a 1-hour window.
6. **Notification Cache:** Unread notification count badges per user.
7. **Merchant & QR Cache:** Dynamic QR token validity and merchant profile lookup.
8. **Beneficiary Cache:** Fast payee validation caching.

---

## ⏰ Background Schedulers

Spring `@Scheduled` background tasks run periodically to automate critical financial operations:

- **`AutoPayScheduler`**: Scans the database every minute for active mandates whose `nextExecutionDate` is due. Automatically acquires distributed locks and triggers transfer execution.
- **`SettlementScheduler`**: Runs periodically (daily at midnight or hourly configurable) to aggregate all unsettled `SUCCESS` merchant transactions into `SettlementBatch` records and process bank payouts.
- **`OutboxScheduler`**: Polls the `OutboxEvent` table every 500ms for `PENDING` events, attempts Kafka publication, updates status to `PUBLISHED`, and logs failures for retries.
- **`Cleanup Jobs`**: Automatically purges expired QR tokens, old processed event records, and expired pending collect requests.

---

## 📂 Project Directory Structure

```
Fintech-UPI-Simulator/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── README.md
├── PROJECT_ARCHITECTURE.md
├── SYSTEM_DESIGN.md
├── API_DOCUMENTATION.md
├── CHANGELOG.md
├── walkthrough.md
├── docs/
│   ├── api/
│   ├── architecture/
│   └── deployment/
├── monitoring/
│   ├── grafana/
│   └── prometheus/
└── src/
    ├── main/
    │   ├── java/com/example/demo/
    │   │   ├── config/             # Spring Security, Redis, Kafka, OpenAPI Config
    │   │   ├── consumer/           # Kafka Event Consumers
    │   │   ├── controller/         # REST API Controllers (16 Controllers)
    │   │   ├── dto/                # Data Transfer Objects & Requests/Responses
    │   │   ├── entity/             # JPA Entities (31 Classes & Enums)
    │   │   ├── events/             # Domain Event Definitions (21 Event Classes)
    │   │   ├── exception/          # Global Exception Handler & Custom Errors
    │   │   ├── metrics/            # Micrometer Prometheus Metrics Services
    │   │   ├── producer/           # Kafka Event Publisher Implementation
    │   │   ├── repository/         # Spring Data JPA Repositories
    │   │   ├── scheduler/          # Background Cron & Outbox Schedulers
    │   │   ├── security/           # JWT Provider, Auth Filter, Security Context
    │   │   └── service/            # Business Interfaces & Service Implementations
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    └── test/                       # Unit & Integration Test Suites
```

---

## 📊 Feature Matrix

| Feature | Module | Status | Description |
| :--- | :--- | :---: | :--- |
| JWT Authentication | Module 1 | ✅ Completed | Secure login, registration, token refresh, RBAC |
| Bank Account Linkage | Module 2 | ✅ Completed | Connect bank accounts, balance check, primary status |
| Custom VPA Management | Module 3 | ✅ Completed | Create custom `@upi` handles, availability check |
| Encrypted UPI PIN | Module 4 | ✅ Completed | BCrypt PIN storage, verification, reset with OTP |
| P2P Money Transfers | Module 5 | ✅ Completed | Atomic transfers, balance validation, receipts |
| Collect Requests | Module 6 | ✅ Completed | Request payment, accept with PIN, decline, auto-expire |
| Docker & Kafka Infra | Module 7 | ✅ Completed | Orchestrated containers, outbox pattern, metrics |
| Merchant Onboarding & QR | Module 8 | ✅ Completed | Merchant profiles, static/dynamic signed QR generation |
| Merchant QR Execution | Module 9 | ✅ Completed | Scanned QR payment execution, merchant credit |
| Beneficiary Management | Module 10 | ✅ Completed | Payee contacts, transfer caps, quick pay |
| AutoPay Mandates | Module 11 | ✅ Completed | Recurring subscription execution, pause/cancel |
| Fraud Risk Engine | Module 12 | ✅ Completed | Velocity scoring, blacklists, high-value alerts |
| Notification Engine | Module 13 | ✅ Completed | Async SMS/Email/Push alerts, user inbox |
| Admin Dashboard APIs | Module 14 | ✅ Completed | System health, user/merchant control, volume analytics |
| Settlement Engine | Module 15 | ✅ Completed | Merchant settlement batching, fees, reconciliation |

---

## 🧪 Testing & Quality Assurance

The codebase features comprehensive unit and integration coverage ensuring financial calculation correctness and security enforcement.

- **Testing Frameworks:** JUnit 5, Mockito, Spring Boot Test, MockMvc, Testcontainers.
- **Unit Coverage:** Complete coverage across all 20+ service implementations (`TransactionServiceImplTest`, `AutoPayServiceImplTest`, `FraudDetectionServiceImplTest`, `SettlementServiceImplTest`, etc.).
- **Integration Coverage:** MockMvc controller tests (`AuthControllerIntegrationTest`, `TransactionControllerIntegrationTest`, `PaymentRequestControllerIntegrationTest`).
- **Code Coverage & JaCoCo:** Configured JaCoCo plugin measuring code branch coverage.

---

## 📈 Observability & Monitoring

The system includes pre-configured monitoring integration:
- **Actuator Health:** `http://localhost:8080/actuator/health`
- **Prometheus Metrics:** `http://localhost:8080/actuator/prometheus`
- **Prometheus Dashboard:** `http://localhost:9090`
- **Grafana Enterprise:** `http://localhost:3000` (Default: `admin` / `admin`)

---

## ⚠️ Known Issues

1. **Swagger OpenAPI Verification:** Swagger UI definitions are configured but undergo ongoing verification for schema edge cases.
2. **Docker Environment Validation:** Full container orchestration scripts require final environment verification on specialized Windows containers.
3. **Frontend Integration:** Frontend web UI is pending integration in Module 16.

---

## 🔮 Next Phase Development

Backend implementation is **100% complete through Module 15**.

### Upcoming Roadmap
- **Module 16:** React / Next.js Web Frontend & Mobile Responsive Interface.
- **Module 17:** Production Hardening (Kubernetes Deployment Helm Charts, Security Audits, High Availability Failover).

---

## 🚀 Getting Started & Local Setup

### Prerequisites
- JDK 17 or higher
- Maven 3.8+
- Docker & Docker Compose

### Step 1: Clone Repository
```bash
git clone https://github.com/sirivally31/Fintech-UPI-Simulator.git
cd Fintech-UPI-Simulator
```

### Step 2: Launch Infrastructure via Docker
```bash
docker compose up -d
```

### Step 3: Build & Run Application
```bash
./mvnw clean install
./mvnw spring-boot:run
```

---

## 👨‍💻 Author & Acknowledgments

**Sirivally Boddula**  
*Final Year B.Tech Computer Science Engineering Student*  
Specializing in Distributed Systems, FinTech Architectures, Spring Boot, Apache Kafka, Redis, and Cloud Native Engineering.

- **GitHub:** [github.com/sirivally31](https://github.com/sirivally31)
