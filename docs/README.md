# Fintech UPI Simulator Documentation Index

Welcome to the comprehensive technical documentation for the **Fintech UPI Payment Simulator Backend**. This production-ready Spring Boot microservice simulates an enterprise Unified Payments Interface (UPI) payment engine with distributed locking, transactional outbox pattern, event-driven Kafka messaging, Redis caching, rate limiting, and containerized deployment.

---

## 📚 Table of Contents

### 🏛️ Architecture & Design
- 🏗️ [System Architecture](architecture/system-architecture.md) — High-level architecture, layer separation, and component design.
- 🔄 [Backend Request Flow](architecture/backend-flow.md) — End-to-end HTTP request processing pipeline from controller to database.
- 🗄️ [Database Design](architecture/database-design.md) — PostgreSQL JPA entities, relationships, constraints, and indexes.
- ⚡ [Event-Driven Architecture](architecture/event-driven-architecture.md) — Transactional Outbox pattern and event publishing lifecycle.
- 🔴 [Redis Architecture](architecture/redis-architecture.md) — OTP caching, rate limiting, and Redisson distributed locks.
- 🚀 [Kafka Architecture](architecture/kafka-architecture.md) — Topic topology, producers, consumers, retry mechanisms, and DLQ handling.
- 💸 [Transaction Flow](architecture/transaction-flow.md) — Double-entry accounting, atomic debits/credits, and UTR reference generation.
- 🛡️ [Security Flow](architecture/security-flow.md) — Spring Security filter chain, JWT authentication, and BCrypt PIN hashing.

### 🔌 API Reference & Documentation
- 🔑 [Authentication API](api/authentication-api.md) — `/api/users/register` and `/api/auth/login` specifications.
- 🏦 [Bank Account API](api/bank-account-api.md) — Management endpoints for linking and querying bank accounts.
- 🆔 [UPI ID API](api/upi-api.md) — VPA handle creation, primary account binding, and updates.
- 💸 [Transaction API](api/transaction-api.md) — P2P money transfers, history retrieval, and transaction summaries.
- 📩 [Payment Request API](api/payment-request-api.md) — Collect request creation, acceptance, rejection, and cancellation lifecycle.
- 📖 [Swagger & OpenAPI Guide](api/swagger-guide.md) — Interactive UI documentation at `/swagger-ui.html`.

### 🐳 Deployment & Operations
- 🐋 [Docker Infrastructure Guide](deployment/docker-guide.md) — Multi-stage Dockerfile and multi-container Docker Compose setup.
- 💻 [Local Development Setup](deployment/local-development.md) — Instructions for running locally with Maven, PostgreSQL, Redis, and Kafka.
- ✅ [Production Readiness Checklist](deployment/production-checklist.md) — Enterprise readiness rules, probes, actuator, and security safeguards.

### 📊 System Diagrams (Mermaid)
- 📐 [Overall System Architecture Diagram](diagrams/architecture.mmd)
- 🔑 [Authentication Flow Sequence Diagram](diagrams/authentication-flow.mmd)
- 💸 [UPI Transaction Flow Sequence Diagram](diagrams/transaction-flow.mmd)
- 📩 [Payment Request Lifecycle Diagram](diagrams/payment-request-flow.mmd)
- ⚡ [Kafka Event Flow Diagram](diagrams/kafka-event-flow.mmd)
- 📦 [Transactional Outbox Pattern Sequence Diagram](diagrams/transactional-outbox-flow.mmd)
- 🔴 [Redis Caching & Lock Flow Diagram](diagrams/redis-cache-flow.mmd)
- 🐳 [Docker Container Topology Diagram](diagrams/docker-architecture.mmd)

### 📸 Media & Visuals
- 🖼️ [Screenshots & UI Showcase](screenshots/README.md) — Directory for UI walkthroughs and OpenAPI screenshots.
