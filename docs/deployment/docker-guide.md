# Multi-Stage Dockerfile & Container Orchestration Guide

## 1. Title & Executive Summary
**Production Docker Containerization, Multi-Stage Builds, & Docker Compose Setup**

This document provides a comprehensive operational guide for building, running, and managing the containerized UPI Simulator infrastructure using multi-stage Docker builds and Docker Compose.

---

## 2. Why the Feature Exists
Containerization eliminates the "works on my machine" operational anti-pattern by bundling the application binary (`app.jar`), Java Runtime Environment (JRE 17), operating system libraries, and configuration files into an immutable container image. Multi-stage builds separate compilation from execution, paring down runtime image size from ~800MB+ to ~200MB.

---

## 3. Architecture Breakdown

### 1. Multi-Stage `Dockerfile` Architecture
- **Stage 1 (Builder)**: Uses `maven:3.9.6-eclipse-temurin-17-alpine`. Copies `pom.xml`, downloads dependencies (`dependency:go-offline`), copies `src/`, and compiles the executable JAR (`mvn package -DskipTests`).
- **Stage 2 (Runner)**: Uses `eclipse-temurin:17-jre-alpine`. Copies *only* the compiled JAR from Stage 1 into `/app/app.jar`. Creates a non-root system user (`appuser`), exposes port `8080`, and sets the container entrypoint.

### 2. Multi-Container Orchestration (`docker-compose.yml`)
Configures 5 interconnected container services on a dedicated bridge network (`upi-network`):
- `postgres` (PostgreSQL 15 Alpine, Port 5432, Volume `postgres_data`)
- `redis` (Redis 7 Alpine, Port 6379)
- `kafka` (Confluent Kafka 7.5.0 KRaft, Port 9092 & 29092, Volume `kafka_data`)
- `kafka-ui` (Provectus Kafka UI, Port 8085)
- `app` (Spring Boot UPI Application, Port 8080)

---

## 4. Commands Reference

### Launching Environment
```bash
docker compose up -d --build
```

### Checking Health Status
```bash
docker compose ps
```

### Viewing Container Logs
```bash
docker compose logs -f app
```

### Tearing Down Infrastructure
```bash
docker compose down -v
```
