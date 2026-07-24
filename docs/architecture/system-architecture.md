# Comprehensive System Architecture

## 1. Title & Executive Summary
**Fintech UPI Payment Simulator — Enterprise System Architecture Specification**

The **UPI Payment Simulator** is a cloud-native, microservice-ready backend application engineered with Java 17 and Spring Boot 3.5.3. It emulates the core functional, transactional, and architectural capabilities of the National Payments Corporation of India (NPCI) Unified Payments Interface (UPI) switch infrastructure. The platform enables real-time Peer-to-Peer (P2P) funds transfers, Virtual Payment Address (VPA) handle resolution, multi-bank account linkage, collect payment requests, distributed concurrency control, and asynchronous event streaming.

---

## 2. Why the Feature Exists
In high-throughput financial environments, processing payments safely requires more than basic CRUD functionality. Real-world UPI ecosystems process billions of transactions per month, requiring:
- **Zero-Loss Transaction Processing**: Financial balances must never suffer from race conditions, double-spending, or partial DB updates.
- **Asynchronous Event-Driven Decoupling**: Downstream subsystems (notifications, analytics, fraud scoring, ledger auditing) must process events without adding latency to the primary payment execution path.
- **Strict Identity & Security Isolation**: Cryptographic authentication (stateless JWT) and salted hash protection for sensitive financial credentials (BCrypt UPI PINs).
- **High Concurrency & Lock Management**: Distributed locks to synchronize concurrent transactions targeting identical bank accounts across clustered application instances.

This system exists to simulate these exact production fintech guarantees within a clean, maintainable Spring Boot reference architecture.

---

## 3. Enterprise Architecture

The platform strictly enforces Domain-Driven Design (DDD), Layered Architecture (N-Tier), and Clean Architecture principles. Each architectural layer has a single, non-overlapping responsibility:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                              │
│  AuthController | BankAccountController | TransactionController | etc.   │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                   SECURITY & INTERCEPTOR LAYER                           │
│     JwtAuthenticationFilter | SecurityConfig | UserDetailsService       │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                   SERVICE & BUSINESS DOMAIN LAYER                        │
│ TransactionServiceImpl | PaymentRequestServiceImpl | UpiPinServiceImpl   │
└──────────────────┬─────────────────┬──────────────────┬──────────────────┘
                   │                 │                  │
                   ▼                 ▼                  ▼
┌──────────────────────┐   ┌──────────────────┐   ┌────────────────────────┐
│  PERSISTENCE LAYER   │   │  CACHING & LOCKS │   │    EVENT BUS LAYER     │
│ Spring Data JPA      │   │ Redis / Redisson │   │ Transactional Outbox   │
│ PostgreSQL 15        │   │ OTP / Rate Limit │   │ Apache Kafka / DLT     │
└──────────────────────┘   └──────────────────┘   └────────────────────────┘
```

### Key Structural Layers:
1. **API Presentation Layer (`com.example.demo.controller`)**:
   - Thin REST controllers handling HTTP requests, input validation (`@Valid`), OpenAPI swagger annotations, and HTTP response encapsulation (`ResponseEntity`). Controllers contain **zero business logic**.
2. **Security Interceptor Layer (`com.example.demo.security`)**:
   - Stateless JWT authentication filter (`JwtAuthenticationFilter`) reading `Authorization: Bearer <token>` headers, decoding claims via `JwtService`, and injecting verified principals into `SecurityContextHolder`.
3. **Business & Domain Logic Layer (`com.example.demo.service`)**:
   - Encapsulates business rules, balance validations, double-entry financial debits/credits, PIN verification, and outbox event persistence. All services use **Constructor Injection** for immutability and testability.
4. **Persistence & Data Access Layer (`com.example.demo.repository`)**:
   - Spring Data JPA repositories communicating with PostgreSQL 15. Transaction boundaries managed via Spring `@Transactional`.
5. **Distributed Caching & Concurrency (`Redis / Redisson`)**:
   - Redis 7 handles OTP storage with TTLs, request rate limiting, and Redisson distributed locks for cross-node account synchronization.
6. **Asynchronous Event-Driven Messaging (`Apache Kafka`)**:
   - Transactional Outbox pattern stores outbox events in PostgreSQL, which an `OutboxScheduler` polls and publishes to Kafka topics using idempotent consumers and Dead Letter Topics (`.DLT`).

---

## 4. How Our Implementation Works

### Core Domain Flow Example: Send Money Transfer
1. **Request Intake**: Client issues `POST /api/transactions/send`.
2. **Authentication**: `JwtAuthenticationFilter` validates token signature and sets `SecurityContext`.
3. **Validation**: Spring validates DTO constraints (`@NotNull`, `@DecimalMin`).
4. **Account Lookup & Authorization**: `TransactionServiceImpl` retrieves sender's bank account using `bankAccountRepository.findByIdAndUser()`, verifying ownership.
5. **VPA & Status Verification**: Looks up receiver VPA (`upiIdRepository.findByUpiId()`) and checks that both receiver VPA and accounts are in `AccountStatus.ACTIVE`.
6. **PIN Check**: Invokes `UpiPinServiceImpl.verifyUpiPin()`, which checks the submitted PIN against the BCrypt hash stored in `bank_accounts.upi_pin`.
7. **Balance Validation**: Validates `senderBalance >= requestedAmount`.
8. **Atomic Balance Update**: Decrements sender balance, increments receiver balance, generates a unique UTR transaction reference (`TXN<yyyyMMddHHmmss><seq>`), and persists transaction record.
9. **Outbox Pattern Dispatch**: Writes `TransactionCompletedEvent` payload into `outbox_events` table within the same database transaction.
10. **Async Event Publishing**: Background `OutboxScheduler` picks up pending outbox rows and streams them to Apache Kafka.

---

## 5. Request Lifecycle (Phase-by-Phase)

```
Phase 1: TCP/HTTP Reception (Tomcat Port 8080)
Phase 2: Security Filter Interception (JwtAuthenticationFilter)
Phase 3: Controller Routing & DTO Validation (@Valid)
Phase 4: Service Transaction Initialization (@Transactional Start)
Phase 5: DB Query Execution & Business Constraint Checking
Phase 6: In-Memory Double-Entry Ledger Modification
Phase 7: PostgreSQL Persistence (Accounts, Transactions, Outbox Records)
Phase 8: Transaction Commit & Outbox Event Emission
Phase 9: Response JSON Serialized to Client (HTTP 200 OK)
```

---

## 6. Database Interaction & Transactions

### Transaction Management (`@Transactional`)
- All modifying service operations run under Spring's declarative transaction manager (`@Transactional(rollbackFor = Exception.class)`).
- **Isolation Level**: Read Committed (PostgreSQL default).
- **Propagation**: `REQUIRED` (joins existing transaction or creates a new one).
- If any validation fails or an unhandled exception occurs (e.g. `SecurityException` for wrong PIN), the transaction is automatically rolled back by Hibernate, discarding all balance updates and outbox insertions.

### Entity Models:
- `User` $\rightarrow$ `BankAccount` (`1:N` relationship)
- `BankAccount` $\rightarrow$ `UpiId` (`1:N` relationship)
- `BankAccount` $\rightarrow$ `Transaction` (`1:N` debits/credits)
- `PaymentRequest` $\rightarrow$ `UpiId` (maps payer/receiver VPAs)
- `OutboxEvent` (stores serialized event payloads)
- `ProcessedEvent` (tracks consumer idempotency)

---

## 7. Spring Boot Components & Stereotypes

- `@SpringBootApplication`: Application bootstrap and component scanning.
- `@RestController` & `@RequestMapping`: Presentation layer API declaration.
- `@Service`: Domain service component stereotype.
- `@Repository`: Data access object mapping to Spring Data JPA.
- `@Configuration` & `@Bean`: Infrastructure beans (`SecurityFilterChain`, `KafkaTemplate`, `RedisTemplate`).
- `@Component`: Custom filters (`JwtAuthenticationFilter`) and schedulers (`OutboxScheduler`).
- `@Scheduled`: Periodic outbox polling tasks.

---

## 8. Security Considerations

- **BCrypt Encryption**: 4/6-digit UPI PINs are hashed using BCrypt before persistence.
- **Insecure Direct Object Reference (IDOR) Protection**: All queries join against the authenticated user ID (`findByIdAndUser`).
- **Stateless Bearer JWTs**: Eliminates CSRF vulnerabilities and session hijacking risk.
- **Non-Root Docker Execution**: Application container executes under unprivileged `USER appuser`.
- **Resource Rate Limiting**: Redis-backed throttling blocks brute-force authentication attacks.

---

## 9. Future Architectural Improvements

- **Multi-Region Database Replication**: Read-replicas for transaction history reads.
- **Service Mesh Integration**: Istio / Envoy for mTLS inter-service encryption.
- **Prometheus & Grafana Observability**: Micrometer metric export for JVM, Connection Pool, and Kafka lag monitoring.
- **Distributed Tracing**: OpenTelemetry / Zipkin correlation headers across Kafka consumer pipelines.
