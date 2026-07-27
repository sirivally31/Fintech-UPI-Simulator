# 📜 Changelog - Enterprise Fintech UPI Simulator

All notable changes, module milestones, architectural enhancements, and feature implementations for the Enterprise Fintech UPI Simulator are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.15.0] - 2026-07-27 - Module 15: Settlement & Reconciliation Engine
### Added
- Implemented `SettlementBatch` and `SettlementEntry` JPA domain entities.
- Created `SettlementService` and `SettlementServiceImpl` to process automated daily/hourly merchant payout batching.
- Platform commission fee calculator deducting dynamic percentage fees before settlement.
- End-of-day bank reconciliation engine producing structured reconciliation audit reports.
- Published Kafka events: `upi.settlement.completed`, `upi.settlement.failed`, `upi.settlement.reconciled`, `upi.settlement.reversed`.
- Added unit tests in `SettlementServiceImplTest`.

---

## [1.14.0] - 2026-07-26 - Module 14: Administrative Dashboard & System Management
### Added
- Implemented `AdminDashboardController` and `AdminDashboardServiceImpl`.
- System volume analytics endpoints returning platform transaction throughput, total INR volume, and success/failure distribution.
- Merchant onboarding verification and approval workflows for administrators.
- User management controls (suspending/activating users, resetting security locks).
- Unit tests added in `AdminDashboardServiceImplTest`.

---

## [1.13.0] - 2026-07-25 - Module 13: Notification & Communication Service
### Added
- Created `Notification` entity, `NotificationChannel`, `NotificationType`, and `NotificationStatus` enums.
- Implemented `NotificationEventConsumer` listening asynchronously to transaction, collect, mandate, and fraud Kafka topics.
- Persistent user notification inbox APIs (`NotificationController`) with read state tracking.
- Idempotency support via `ProcessedEvent` repository to prevent duplicate alert dispatches.
- Unit tests added in `NotificationServiceImplTest`.

---

## [1.12.0] - 2026-07-24 - Module 12: Fraud Detection & Risk Engine
### Added
- Implemented `FraudRule` entity and `FraudRuleType`, `FraudDecision` enums.
- Created `FraudDetectionServiceImpl` computing composite risk scores based on velocity counters, transaction amount thresholds, and blacklists.
- Redis-backed sliding velocity window tracking VPA transaction counts per hour.
- Kafka events: `upi.fraud.detected`, `upi.fraud.blocked`, `upi.fraud.high-risk`.
- Unit tests added in `FraudDetectionServiceImplTest`.

---

## [1.11.0] - 2026-07-23 - Module 11: AutoPay & Scheduled Payments
### Added
- Implemented `AutoPay` domain entity and `AutoPayFrequency`, `AutoPayStatus` enums.
- Created `AutoPayServiceImpl` for recurring mandate subscription setup, execution, pause, and cancellation.
- `AutoPayScheduler` background job running every 60 seconds to execute due mandates automatically with distributed lock protection.
- Kafka events: `upi.autopay.created`, `upi.autopay.executed`, `upi.autopay.failed`, `upi.autopay.cancelled`.
- Unit tests added in `AutoPayServiceImplTest`.

---

## [1.10.0] - 2026-07-22 - Module 10: Beneficiary Management
### Added
- Implemented `Beneficiary` domain entity and `BeneficiaryService`.
- APIs for adding payees, listing saved contacts, updating nicknames, and removing contacts.
- Transfer cap enforcement for newly added unverified beneficiaries.
- Kafka events: `upi.beneficiary.added`, `upi.beneficiary.updated`, `upi.beneficiary.deleted`.
- Unit tests added in `BeneficiaryServiceImplTest`.

---

## [1.9.0] - 2026-07-21 - Module 9: QR Payment Execution
### Added
- Implemented `QrPaymentServiceImpl` processing scan-and-pay merchant transactions.
- Validation for static and dynamic signed QR tokens, checking expiry timestamps and pre-filled payment amounts.
- Atomic payment credit to merchant settlement VPAs with instant webhook event trigger.
- Kafka event: `upi.qr.payment.success`.
- Unit tests added in `QrPaymentServiceImplTest`.

---

## [1.8.0] - 2026-07-20 - Module 8: Merchant QR Payment System
### Added
- Implemented `Merchant` and `MerchantQr` entities.
- Created `MerchantServiceImpl` and `QrServiceImpl` for merchant onboarding and category (MCC) management.
- Static and dynamic QR payload token generator signed with HMAC keys.
- Kafka event: `upi.qr.created`.

---

## [1.7.0] - 2026-07-19 - Module 7: Enterprise Infrastructure
### Added
- Multi-container `docker-compose.yml` orchestrating Spring Boot, PostgreSQL 15, Redis 7, Kafka + Zookeeper, Kafka UI, Prometheus, and Grafana.
- Integrated **Transactional Outbox Pattern** (`OutboxEvent` entity, `OutboxPublisherServiceImpl`, `OutboxScheduler`) eliminating Dual Write problems.
- Distributed locking service (`DistributedLockService`) backed by Redis.
- Micrometer Prometheus metrics export at `/actuator/prometheus` and auto-provisioned Grafana dashboards.

---

## [1.6.0] - 2026-07-18 - Module 6: Payment Requests
### Added
- Implemented `PaymentRequest` entity and `PaymentRequestStatus` state machine.
- Collect request APIs for initiating, listing pending, accepting (with PIN validation), declining, and revoking payment requests.
- Automatic collect request expiry background scheduler.
- Integration tests added in `PaymentRequestControllerIntegrationTest`.

---

## [1.5.0] - 2026-07-17 - Module 5: Money Transfer
### Added
- Core transaction engine `TransactionServiceImpl` executing atomic P2P balance transfers.
- Database transaction isolation and optimistic locking (`@Version`) on bank accounts.
- Transaction history, filter queries, and PDF transaction receipt generation support.
- Unit and integration tests added in `TransactionServiceImplTest` and `TransactionControllerIntegrationTest`.

---

## [1.4.0] - 2026-07-16 - Module 4: UPI PIN Management
### Added
- Implemented `UpiPinServiceImpl` managing encrypted 4-to-6 digit UPI PINs.
- Secure PIN hashing using BCrypt with salt generation.
- PIN verification, PIN change, and OTP-authenticated PIN reset flows.

---

## [1.3.0] - 2026-07-15 - Module 3: UPI ID Management
### Added
- Implemented `UpiId` entity and `UpiIdServiceImpl`.
- Virtual Payment Address (`vpa`) creation, primary handle selection, and VPA availability check APIs.
- Integration tests added in `UpiIdControllerIntegrationTest`.

---

## [1.2.0] - 2026-07-14 - Module 2: Bank Account Management
### Added
- Implemented `BankAccount` entity and `BankAccountServiceImpl`.
- Core bank account linking, balance inquiry, account type management, and status validation.
- Integration tests added in `BankAccountControllerIntegrationTest`.

---

## [1.1.0] - 2026-07-13 - Module 1: Authentication & JWT
### Added
- Implemented `User` entity, `UserService`, `AuthController`, and Spring Security configuration.
- JWT token generation, signature validation, refresh token handling, and BCrypt password encryption.
- Integration tests added in `AuthControllerIntegrationTest`.

---

## [0.1.0] - 2026-07-10 - Initial Project Setup
### Added
- Initialized Spring Boot 3 project structure, Maven configuration, parent dependencies, database migrations, and basic configuration files.
