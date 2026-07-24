# 🚀 Fintech UPI Payment Simulator

An enterprise-grade backend system that simulates a modern UPI payment platform inspired by real-world fintech applications such as PhonePe, Google Pay, Paytm, and Razorpay.

This project demonstrates secure payment processing, distributed systems, event-driven architecture, reliable messaging, caching, Dockerized deployment, and production-ready backend engineering using Spring Boot.

---

# 📖 Overview

The goal of this project is to build a production-inspired UPI payment platform rather than a simple CRUD application.

The system implements:

- Secure Authentication
- Bank Account Management
- UPI IDs
- UPI PIN Security
- Money Transfers
- Collect Requests
- Kafka Event Streaming
- Redis Caching
- Distributed Locking
- Transactional Outbox Pattern
- Docker Deployment
- Enterprise Testing

The project follows enterprise software engineering principles used in modern fintech systems.

---

# 🏗 Architecture

```

                +----------------------+
                |      Frontend        |
                |   (React / Next.js)  |
                +----------+-----------+
                           |
                           |
                    REST APIs (JWT)
                           |
                           v
             +----------------------------+
             | Spring Boot Backend        |
             +----------------------------+
                 |      |        |
                 |      |        |
                 |      |        |
            PostgreSQL Redis   Kafka
                 |      |        |
                 |      |        |
          Transactions Cache Events

```

---

# ✨ Features

## Authentication

- JWT Authentication
- BCrypt Password Encryption
- Spring Security
- Stateless Authentication
- Role-Based Authorization

---

## User Module

- User Registration
- User Login
- Secure Authentication
- User Profile Management

---

## Bank Account Module

- Create Bank Account
- Update Bank Account
- Delete Bank Account
- Link Multiple Accounts
- Ownership Validation
- Account Status Management

---

## UPI Module

- Create UPI IDs
- Manage UPI IDs
- Multiple UPI IDs
- Active / Inactive Status

---

## UPI PIN

- Set PIN
- Change PIN
- Verify PIN
- Encrypted PIN Storage

---

## Transaction Module

- Send Money
- Balance Validation
- Transaction History
- Transaction Summary
- Atomic Transactions
- ACID Compliance

---

## Collect Request Module

- Request Money
- Accept Request
- Reject Request
- Cancel Request
- Expiry Handling

Supported States

- Pending
- Accepted
- Rejected
- Cancelled
- Expired

---

# ⚡ Event Driven Architecture

The application publishes events whenever important business actions occur.

Examples

- Transaction Completed
- Payment Request Created
- Payment Request Accepted
- Payment Request Rejected

Events are published through Kafka and consumed asynchronously by downstream services.

---

# 🔥 Kafka Integration

Implemented Features

- Kafka Producer
- Kafka Consumer
- Event Publisher
- Topic Management
- Dead Letter Topics
- Retry Strategy
- Reliable Messaging

---

# 📦 Transactional Outbox Pattern

The application implements the Transactional Outbox Pattern to eliminate the Dual Write Problem.

Benefits

- Reliable Event Publishing
- No Lost Events
- Eventual Consistency
- Enterprise Messaging

---

# ⚡ Redis Integration

Implemented Redis Features

- OTP Cache
- Distributed Locks
- Idempotency
- Rate Limiting
- High-Speed Cache

---

# 🐳 Docker

Dockerized Infrastructure

- Spring Boot
- PostgreSQL
- Redis
- Kafka
- Kafka UI

Includes

- Docker Compose
- Health Checks
- Persistent Volumes
- Production Startup Ordering

---

# 📚 API Documentation

Swagger OpenAPI is integrated.

Available after running the application.

```
http://localhost:8080/swagger-ui/index.html
```

---

# ❤️ Health Monitoring

Spring Boot Actuator is configured.

Health Endpoint

```
http://localhost:8080/actuator/health
```

---

# 🛡 Security

Implemented Security Features

- JWT Authentication
- BCrypt Encryption
- Authorization Filters
- Ownership Validation
- IDOR Protection
- Secure REST APIs

---

# 🧪 Testing

Testing Stack

- JUnit 5
- Mockito
- Spring Boot Test
- MockMvc
- Testcontainers

Current Coverage

- Service Layer Unit Tests
- REST API Integration Tests
- Kafka Components
- Business Logic Validation

---

# 🏛 Design Patterns Used

- Layered Architecture
- Repository Pattern
- DTO Pattern
- Dependency Injection
- Builder Pattern
- Event Driven Architecture
- Transactional Outbox
- Distributed Locking
- Idempotency

---

# 📂 Project Structure

```

src

├── config

├── controller

├── consumer

├── dto

├── entity

├── events

├── exception

├── producer

├── repository

├── scheduler

├── security

├── service

│ └── impl

└── resources

```

---

# 🛠 Tech Stack

Backend

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Hibernate

Database

- PostgreSQL

Messaging

- Apache Kafka

Caching

- Redis

Documentation

- Swagger OpenAPI

Build

- Maven

Deployment

- Docker
- Docker Compose

Testing

- JUnit
- Mockito
- MockMvc
- Testcontainers

---

# 📈 Current Progress

| Module | Status |
|----------|--------|
| Authentication | ✅ |
| Bank Accounts | ✅ |
| UPI IDs | ✅ |
| UPI PIN | ✅ |
| Transactions | ✅ |
| Payment Requests | ✅ |
| Kafka | ✅ |
| Reliable Messaging | ✅ |
| Transactional Outbox | ✅ |
| Redis | ✅ |
| Docker | ✅ |
| Testing | ✅ |
| Production Readiness | ✅ |

---

# 🚀 Getting Started

Clone Repository

```bash
git clone https://github.com/sirivally31/Fintech-UPI-Simulator.git
```

Go into project

```bash
cd Fintech-UPI-Simulator
```

Build

```bash
./mvnw clean install
```

Run

```bash
./mvnw spring-boot:run
```

Run with Docker

```bash
docker compose up --build
```

---

# 🔮 Future Enhancements

- Email Notifications
- SMS Notifications
- Push Notifications
- Fraud Detection Engine
- Merchant APIs
- Admin Dashboard
- Prometheus Monitoring
- Grafana Dashboards
- Kubernetes Deployment
- CI/CD Pipeline
- AWS Deployment
- ELK Logging

---

# 👨‍💻 Author

**Sirivally Boddula**

Final Year B.Tech Computer Science Engineering Student

Backend Developer passionate about Distributed Systems, FinTech, Spring Boot, Kafka, Redis, Docker, and Microservices.

GitHub

https://github.com/sirivally31

---

If you found this project useful, consider giving it a ⭐.
