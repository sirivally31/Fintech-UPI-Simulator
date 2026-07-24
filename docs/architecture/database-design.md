# Relational Database Schema & Persistence Architecture

## 1. Title & Executive Summary
**PostgreSQL Entity-Relationship Schema, Indexing Strategy, & JPA Persistence Design**

This document provides an exhaustive reference of the relational database design powering the UPI Payment Simulator. It covers table schemas, column data types, foreign key constraints, indexing strategies, JPA object-relational mapping rules, and transactional outbox table specifications in PostgreSQL 15.

---

## 2. Why the Feature Exists
Financial ledgers demand absolute structural data integrity:
- **Floating-Point Imprecision Defense**: Standard IEEE floating-point types (`FLOAT`, `DOUBLE`) introduce binary rounding errors (e.g. `0.1 + 0.2 = 0.30000000000000004`). Database schemas must mandate high-precision decimal representation (`numeric(15,2)`).
- **Referential & Unique Constraints**: Database-level unique indexes guarantee that no two users share a phone number or VPA, and no duplicate transaction reference (UTR) can ever be inserted, even under heavy parallel load.
- **Audit Traceability**: Immutable creation and modification timestamps (`created_at`, `updated_at`, `responded_at`) track event timing across all domain objects.

---

## 3. Enterprise Schema Architecture

```
                       ┌──────────────────────┐
                       │        users         │
                       ├──────────────────────┤
                       │ PK id (BIGSERIAL)    │
                       │    name (VARCHAR)    │
                       │ UQ phoneNumber (15)  │
                       │ UQ upiId (50)        │
                       │    pin (BCrypt Hash) │
                       │    balance (NUMERIC) │
                       └──────────┬───────────┘
                                  │ 1
                                  │
                                  ▼ *
                       ┌──────────────────────┐
                       │    bank_accounts     │
                       ├──────────────────────┤
                       │ PK id (BIGSERIAL)    │
                       │ FK user_id (BIGINT)  │
                       │ UQ accountNumber(20) │
                       │    bankName (VARCHAR)│
                       │    ifscCode (11)     │
                       │    accountType(ENUM) │
                       │    balance (NUMERIC) │
                       │    status (ENUM)     │
                       │    upi_pin (BCrypt)  │
                       └──────────┬───────────┘
                                  │ 1
                                  │
                                  ▼ *
                       ┌──────────────────────┐
                       │       upi_ids        │
                       ├──────────────────────┤
                       │ PK id (BIGSERIAL)    │
                       │ FK bank_account_id   │
                       │ UQ upiId (VARCHAR)   │
                       │    isPrimary (BOOL)  │
                       │    status (ENUM)     │
                       └──────────┬───────────┘
                                  │
         ┌────────────────────────┴────────────────────────┐
         │ 1                                               │ 1
         ▼ * (Sender / Receiver VPAs)                      ▼ * (Sender / Receiver VPAs)
┌──────────────────────────────────┐             ┌──────────────────────────────────┐
│           transactions           │             │         payment_requests         │
├──────────────────────────────────┤             ├──────────────────────────────────┤
│ PK id (BIGSERIAL)                │             │ PK id (BIGSERIAL)                │
│ UQ transactionReference (100)    │             │ UQ requestReference (100)        │
│ FK sender_bank_account_id        │             │ FK sender_upi_id (Payer)         │
│ FK receiver_bank_account_id      │             │ FK receiver_upi_id (Requester)   │
│ FK sender_upi_id                 │             │    amount (NUMERIC(15,2))        │
│ FK receiver_upi_id               │             │    note (VARCHAR(255))           │
│    amount (NUMERIC(15,2))        │             │    status (ENUM)                 │
│    remarks (VARCHAR(255))        │             │    createdAt (TIMESTAMP)         │
│    status (ENUM)                 │             │    expiresAt (TIMESTAMP)         │
│    createdAt (TIMESTAMP)         │             │    respondedAt (TIMESTAMP)       │
└──────────────────────────────────┘             └──────────────────────────────────┘

┌──────────────────────────────────┐             ┌──────────────────────────────────┐
│          outbox_events           │             │         processed_events         │
├──────────────────────────────────┤             ├──────────────────────────────────┤
│ PK event_id (UUID)               │             │ PK event_id (UUID)               │
│    aggregate_type (VARCHAR)      │             │    event_type (VARCHAR)          │
│    aggregate_id (BIGINT)         │             │    processed_at (TIMESTAMP)      │
│    event_type (VARCHAR)          │             └──────────────────────────────────┘
│    correlation_id (VARCHAR)      │
│    payload (JSONB / TEXT)        │
│    status (PENDING/PROCESSED)    │
│    created_at (TIMESTAMP)        │
└──────────────────────────────────┘
```

---

## 4. Entity Specifications & Field Mappings

### 1. `users` Table (`com.example.demo.entity.User`)
| Column Name | SQL Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Auto-increment primary key |
| `name` | `VARCHAR(255)` | `NOT NULL` | Full user name |
| `phone_number` | `VARCHAR(15)` | `NOT NULL`, `UNIQUE` | 10-digit mobile number |
| `upi_id` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` | Default system VPA |
| `pin` | `VARCHAR(255)` | `NOT NULL` | BCrypt password hash |
| `balance` | `NUMERIC(15,2)`| `NOT NULL` | System wallet balance |

### 2. `bank_accounts` Table (`com.example.demo.entity.BankAccount`)
| Column Name | SQL Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Primary key |
| `user_id` | `BIGINT` | `FOREIGN KEY (users.id)`, `NOT NULL` | Owning user ID |
| `account_number` | `VARCHAR(20)`| `NOT NULL`, `UNIQUE` | 10-digit generated account number |
| `bank_name` | `VARCHAR(255)`| `NOT NULL` | Commercial bank name (e.g. HDFC) |
| `ifsc_code` | `VARCHAR(11)` | `NOT NULL` | Bank IFSC Code |
| `account_type` | `VARCHAR(20)` | `NOT NULL` | `SAVINGS` or `CURRENT` |
| `balance` | `NUMERIC(15,2)`| `NOT NULL` | Available bank account balance |
| `status` | `VARCHAR(20)` | `NOT NULL` | `ACTIVE` or `BLOCKED` |
| `upi_pin` | `VARCHAR(255)`| `NULLABLE` | BCrypt-hashed 4/6 digit UPI PIN |
| `created_at` | `TIMESTAMP` | `NOT NULL` | Creation timestamp |

### 3. `upi_ids` Table (`com.example.demo.entity.UpiId`)
| Column Name | SQL Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `id` | `BIGSERIAL` | `PRIMARY KEY` | Primary key |
| `bank_account_id` | `BIGINT` | `FOREIGN KEY (bank_accounts.id)` | Linked bank account |
| `upi_id` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` | Full VPA string (e.g. `john@oksbi`) |
| `is_primary` | `BOOLEAN` | `NOT NULL` | Primary VPA indicator for account |
| `status` | `VARCHAR(20)` | `NOT NULL` | `ACTIVE` or `INACTIVE` |

---

## 5. Transactional Integrity & Relational Rules

1. **Self-Transfer Prevention**: Guaranteed at application layer; database integrity enforced via foreign key consistency.
2. **Cascade Rules**: Bank accounts are bound to users with `@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)`.
3. **Optimistic & Pessimistic Locking**:
   - Outbox processing queries outbox events in batch.
   - Financial ledger updates lock rows during balance modification (`SELECT ... FOR UPDATE`).

---

## 6. Spring Data JPA Repositories

- `UserRepository`: `findByUpiId()`, `findByPhoneNumber()`, `existsByUpiId()`.
- `BankAccountRepository`: `findByIdAndUser()`, `findAllByUser()`, `existsByAccountNumber()`.
- `UpiIdRepository`: `findByUpiId()`, `findByBankAccount()`, `findByBankAccountAndIsPrimaryTrue()`.
- `TransactionRepository`: `findByTransactionReference()`, `findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc()`.
- `PaymentRequestRepository`: `findByRequestReference()`, `findBySenderUpiIdOrderByCreatedAtDesc()`.

---

## 7. Security & Precision Best Practices

- `BigDecimal` mandates zero loss in monetary arithmetic.
- `@Enumerated(EnumType.STRING)` protects database readable integrity against Java Enum ordinal reordering.
- Cryptographic hash columns (`upi_pin`, `pin`) allocated 255 characters to accommodate standard 60-character BCrypt hashes with future algorithm headroom.
