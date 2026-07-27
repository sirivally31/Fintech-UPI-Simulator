# 📐 System Design & Data Models - Enterprise Fintech UPI Simulator

This document presents the detailed domain system design, database schemas, entity relationship diagrams (ERD), finite state machine (FSM) models, indexing strategies, and algorithm specifications powering Modules 1 to 15.

---

## 📖 Table of Contents
1. [Domain Model & Entity Schema](#1-domain-model--entity-schema)
2. [Entity Relationship Diagrams (ERD)](#2-entity-relationship-diagrams-erd)
3. [Finite State Machines (FSM)](#3-finite-state-machines-fsm)
4. [Database Indexing & Optimization Strategy](#4-database-indexing--optimization-strategy)
5. [Concurrency Control & Anti-Double-Spend Design](#5-concurrency-control--anti-double-spend-design)
6. [Fraud Engine & Risk Scoring Algorithms](#6-fraud-engine--risk-scoring-algorithms)
7. [Merchant Settlement & Reconciliation Engine](#7-merchant-settlement--reconciliation-engine)

---

## 1. Domain Model & Entity Schema

The database schema is designed for 3rd Normal Form (3NF) compliance with optimized foreign keys and indexes.

### 1.1 Core User & Account Domain

#### `users` Table
Stores account holder profile details and authentication credentials.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Unique internal user ID |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| `password` | VARCHAR(100) | NOT NULL | BCrypt password hash |
| `email` | VARCHAR(100) | UNIQUE, NOT NULL | Registered email address |
| `phone_number` | VARCHAR(15) | UNIQUE, NOT NULL | Mobile number for UPI lookup |
| `full_name` | VARCHAR(100) | NOT NULL | User's legal name |
| `roles` | VARCHAR(50) | NOT NULL | Roles string (`ROLE_USER`, `ROLE_ADMIN`) |
| `status` | VARCHAR(20) | NOT NULL | Account status (`ACTIVE`, `SUSPENDED`) |
| `created_at` | TIMESTAMP | NOT NULL | Registration timestamp |
| `updated_at` | TIMESTAMP | NOT NULL | Last update timestamp |

#### `bank_accounts` Table
Represents core bank accounts linked to the UPI switch.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Unique account record ID |
| `account_number` | VARCHAR(20) | UNIQUE, NOT NULL | Core bank account number |
| `ifsc_code` | VARCHAR(11) | NOT NULL | Bank branch IFSC code |
| `bank_name` | VARCHAR(100) | NOT NULL | Bank institution name |
| `account_type` | VARCHAR(20) | NOT NULL | `SAVINGS`, `CURRENT` |
| `balance` | DECIMAL(15,2) | NOT NULL | Account balance in INR |
| `is_primary` | BOOLEAN | NOT NULL | Default account flag |
| `status` | VARCHAR(20) | NOT NULL | `ACTIVE`, `FROZEN` |
| `version` | BIGINT | NOT NULL | JPA Optimistic locking version |
| `user_id` | BIGINT | FOREIGN KEY (`users.id`) | Account owner reference |

#### `upi_ids` Table
Stores Virtual Payment Addresses (`vpa`) mapped to linked bank accounts.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Unique VPA ID |
| `vpa` | VARCHAR(50) | UNIQUE, NOT NULL | UPI Handle (e.g. `john@upi`) |
| `pin_hash` | VARCHAR(100) | NOT NULL | BCrypt hash of 4-6 digit UPI PIN |
| `is_primary` | BOOLEAN | NOT NULL | Primary VPA flag |
| `status` | VARCHAR(20) | NOT NULL | `ACTIVE`, `INACTIVE` |
| `version` | BIGINT | NOT NULL | Optimistic locking version |
| `bank_account_id`| BIGINT | FOREIGN KEY (`bank_accounts.id`)| Linked bank account |
| `user_id` | BIGINT | FOREIGN KEY (`users.id`) | VPA owner reference |

---

### 1.2 Transactions & Payment Requests

#### `transactions` Table
Immutable ledger of all money movement across the platform.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Internal transaction ID |
| `txn_reference` | VARCHAR(36) | UNIQUE, NOT NULL | UUID transaction reference |
| `sender_vpa` | VARCHAR(50) | NOT NULL | Sender's VPA |
| `receiver_vpa` | VARCHAR(50) | NOT NULL | Receiver's VPA |
| `amount` | DECIMAL(15,2) | NOT NULL | Transaction amount |
| `txn_type` | VARCHAR(20) | NOT NULL | `P2P`, `P2M`, `COLLECT`, `AUTOPAY` |
| `status` | VARCHAR(20) | NOT NULL | `SUCCESS`, `FAILED`, `PENDING` |
| `failure_reason` | VARCHAR(255) | NULLABLE | Reason string if transaction failed |
| `remarks` | VARCHAR(255) | NULLABLE | Transaction note |
| `created_at` | TIMESTAMP | NOT NULL | Execution timestamp |

#### `payment_requests` Table
Collect payment requests requested by users.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Request ID |
| `request_reference`| VARCHAR(36) | UNIQUE, NOT NULL | UUID request reference |
| `requester_vpa` | VARCHAR(50) | NOT NULL | Collector's VPA |
| `payer_vpa` | VARCHAR(50) | NOT NULL | Payer's VPA |
| `amount` | DECIMAL(15,2) | NOT NULL | Requested amount |
| `status` | VARCHAR(20) | NOT NULL | `PENDING`, `ACCEPTED`, `REJECTED`, `EXPIRED`, `CANCELLED` |
| `expiry_at` | TIMESTAMP | NOT NULL | Expiry timestamp |
| `created_at` | TIMESTAMP | NOT NULL | Creation timestamp |

---

### 1.3 Merchant, QR & Beneficiary Subsystems

#### `merchants` Table
Stores onboarded merchant profiles.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Merchant ID |
| `merchant_code` | VARCHAR(30) | UNIQUE, NOT NULL | Unique business code |
| `business_name` | VARCHAR(100) | NOT NULL | Registered company name |
| `mcc` | VARCHAR(10) | NOT NULL | Merchant Category Code |
| `settlement_vpa` | VARCHAR(50) | NOT NULL | Settlement VPA/Account |
| `status` | VARCHAR(20) | NOT NULL | `VERIFIED`, `PENDING`, `BLOCKED` |

#### `merchant_qrs` Table
Dynamic and Static QR codes generated for merchants.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | QR ID |
| `qr_token` | VARCHAR(36) | UNIQUE, NOT NULL | UUID token encoded into QR payload |
| `qr_type` | VARCHAR(10) | NOT NULL | `STATIC`, `DYNAMIC` |
| `amount` | DECIMAL(15,2) | NULLABLE | Pre-filled amount for dynamic QR |
| `is_active` | BOOLEAN | NOT NULL | Active status flag |
| `expires_at` | TIMESTAMP | NULLABLE | Expiry time for dynamic QR |
| `merchant_id` | BIGINT | FOREIGN KEY (`merchants.id`) | Owning merchant |

#### `beneficiaries` Table
Saved contact payees associated with users.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Beneficiary ID |
| `payee_name` | VARCHAR(100) | NOT NULL | Beneficiary full name |
| `payee_vpa` | VARCHAR(50) | NOT NULL | Target VPA |
| `nickname` | VARCHAR(50) | NULLABLE | User-assigned contact alias |
| `user_id` | BIGINT | FOREIGN KEY (`users.id`) | Owning user ID |

---

### 1.4 AutoPay, Fraud, Notifications & Outbox

#### `autopays` Table
Standing orders and recurring mandate subscriptions.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Mandate ID |
| `mandate_reference`| VARCHAR(36) | UNIQUE, NOT NULL | Mandate reference code |
| `frequency` | VARCHAR(20) | NOT NULL | `DAILY`, `WEEKLY`, `MONTHLY` |
| `amount` | DECIMAL(15,2) | NOT NULL | Recurring execution amount |
| `max_amount` | DECIMAL(15,2) | NOT NULL | Upper limit cap |
| `next_execution_date`| TIMESTAMP | NOT NULL | Scheduled execution time |
| `status` | VARCHAR(20) | NOT NULL | `ACTIVE`, `PAUSED`, `CANCELLED` |
| `user_id` | BIGINT | FOREIGN KEY (`users.id`) | Mandate subscriber |

#### `settlement_batches` Table
Aggregated merchant payout batches.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Batch ID |
| `batch_reference` | VARCHAR(36) | UNIQUE, NOT NULL | Batch reference code |
| `total_amount` | DECIMAL(15,2) | NOT NULL | Gross transaction sum |
| `net_amount` | DECIMAL(15,2) | NOT NULL | Net payout after fees |
| `fee_amount` | DECIMAL(15,2) | NOT NULL | Platform commission fee |
| `status` | VARCHAR(20) | NOT NULL | `PENDING`, `COMPLETED`, `FAILED` |
| `merchant_id` | BIGINT | FOREIGN KEY (`merchants.id`) | Target merchant |

#### `outbox_events` Table
Transactional outbox pattern event store.
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PRIMARY KEY, AUTO_INC | Outbox Event ID |
| `aggregate_type` | VARCHAR(50) | NOT NULL | Domain object type |
| `aggregate_id` | VARCHAR(50) | NOT NULL | Unique aggregate reference |
| `event_type` | VARCHAR(50) | NOT NULL | Event name (e.g. `TransactionCompletedEvent`)|
| `payload` | TEXT | NOT NULL | JSON serialized event data |
| `status` | VARCHAR(20) | NOT NULL | `PENDING`, `PUBLISHED`, `FAILED` |
| `retry_count` | INT | NOT NULL | Retry counter |
| `created_at` | TIMESTAMP | NOT NULL | Event creation timestamp |

---

## 2. Entity Relationship Diagrams (ERD)

```
  +-------------------+        1:N        +-------------------+        1:N        +-------------------+
  |       User        | ----------------> |    BankAccount    | ----------------> |       UpiId       |
  +-------------------+                   +-------------------+                   +-------------------+
    |         |         \                       |                                       |
    | 1:N     | 1:N      \ 1:N                  | 1:N                                   | 1:N (Sender/Receiver)
    v         v           v                     v                                       v
+-------+ +-------+ +----------+          +-----------+                           +-----------+
|Notif. | |AutoPay| |Benefic.  |          |MerchantQr |                           |Transaction|
+-------+ +-------+ +----------+          +-----------+                           +-----------+
                                                ^                                       |
                                                | 1:N                                   | 1:N
                                          +-----------+                           +-----------+
                                          | Merchant  | ------------------------> |Settlement |
                                          +-----------+         1:N               +-----------+
```

---

## 3. Finite State Machines (FSM)

### 3.1 Collect Payment Request State Machine

```
               +-----------+
               |  PENDING  |
               +-----+-----+
                     |
     +---------------+---------------+---------------+
     | (User Pays)   | (User Rejects)| (Requester    | (Cron Expiry)
     v               v               | Cancels)      v
+----+------+  +-----+-----+         v         +-----+-----+
| ACCEPTED  |  | REJECTED  |   +-----+-----+   |  EXPIRED  |
+-----------+  +-----------+   | CANCELLED |   +-----------+
                               +-----------+
```

### 3.2 Merchant Settlement Batch State Machine

```
              +-----------+
              |  PENDING  |
              +-----+-----+
                    |
          +---------+---------+
          v                   v
   +--------------+    +--------------+
   |  COMPLETED   |    |    FAILED    |
   +------+-------+    +--------------+
          |
          v (Reconciled / Reversed)
   +--------------+
   |  RECONCILED  |
   +--------------+
```

---

## 4. Database Indexing & Optimization Strategy

To maintain sub-50ms query response times under high transaction load, indices have been constructed across high-frequency lookup fields:

```sql
-- User and Authentication Indexes
CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE UNIQUE INDEX idx_users_phone ON users(phone_number);

-- Bank Account and VPA Indexes
CREATE UNIQUE INDEX idx_bank_accounts_number ON bank_accounts(account_number);
CREATE UNIQUE INDEX idx_upi_ids_vpa ON upi_ids(vpa);
CREATE INDEX idx_upi_ids_user ON upi_ids(user_id);

-- Transaction Ledger Indexes
CREATE UNIQUE INDEX idx_txn_reference ON transactions(txn_reference);
CREATE INDEX idx_txn_sender ON transactions(sender_vpa, created_at DESC);
CREATE INDEX idx_txn_receiver ON transactions(receiver_vpa, created_at DESC);

-- Outbox Poller Index
CREATE INDEX idx_outbox_status ON outbox_events(status, created_at ASC) WHERE status = 'PENDING';
```

---

## 5. Concurrency Control & Anti-Double-Spend Design

The system enforces a **dual-lock safeguard** against concurrent transactions:

1. **Pessimistic Redis Distributed Lock (`DistributedLockService`):**
   When debiting `Account A` to credit `Account B`, locks are acquired in sorted account number order (preventing deadlocks):
   ```java
   String lockKey1 = "lock:account:" + Math.min(accountA, accountB);
   String lockKey2 = "lock:account:" + Math.max(accountA, accountB);
   ```
2. **JPA Optimistic Locking (`@Version`):**
   `BankAccount` and `UpiId` contain an integer `@Version` field. If another thread mutates the record in PostgreSQL between read and commit, Hibernate throws an `OptimisticLockException` preventing overwrites.

---

## 6. Fraud Engine & Risk Scoring Algorithms

The Fraud Detection Engine (`FraudDetectionServiceImpl`) computes a composite risk score prior to authorizing transactions:

```
Risk Score = (W_velocity * VelocityScore) + (W_amount * AmountScore) + BlacklistScore
```

- **Rule 1 (High Velocity):** Checks Redis counter `fraud:velocity:{vpa}`. > 5 transfers in 10 minutes adds +40 risk points.
- **Rule 2 (High Value Threshold):** Transfers > ₹50,000 add +30 risk points.
- **Rule 3 (Blacklisted VPA):** Blacklisted accounts instantly return +100 risk points.

### Risk Action Escalation:
- **Score < 50:** `ALLOWED` – Transaction proceeds normally.
- **50 <= Score < 80:** `FLAGGED` – Allowed but publishes `HighRiskTransactionEvent` to Kafka for admin auditing.
- **Score >= 80:** `BLOCKED` – Transaction is declined immediately and `FraudBlockedEvent` is published.

---

## 7. Merchant Settlement & Reconciliation Engine

Merchant payments accumulate throughout the day in `P2M` transaction logs.

1. **Batching:** `SettlementScheduler` groups all unsettled transactions where `merchant_id = M` and `created_at` falls in the settlement window.
2. **Fee Deduction:** A platform commission (e.g. 1.5% default) is calculated:
   ```
   fee_amount = total_amount * 0.015
   net_amount = total_amount - fee_amount
   ```
3. **Execution:** Transfers `net_amount` into the merchant's registered settlement bank account.
4. **Reconciliation:** End-of-day reconciliation verifies that `SUM(SettlementEntries) == SettlementBatch.total_amount`.

---
*For architectural component layouts, consult [PROJECT_ARCHITECTURE.md](file:///e:/UPI%20Simulator/PROJECT_ARCHITECTURE.md).*
