# Local Developer Setup & Testing Guide

## 1. Title & Executive Summary
**Local Setup, Dependency Execution, & Test Suite Execution**

This document provides developer onboarding instructions for cloning, compiling, testing, and running the UPI Simulator on local development workstations.

---

## 2. Environment Prerequisites
- Java Development Kit (JDK 17)
- Maven 3.9+ (or embedded `./mvnw` wrapper)
- Docker Desktop or Podman

---

## 3. Step-by-Step Developer Workflow

### Step 1: Clone Repository & Compile Code
```bash
# Compile classes without running tests
.\mvnw.cmd clean test-compile
```

### Step 2: Launch Local Dependencies
```bash
# Launch PostgreSQL, Redis, and Kafka in background
docker compose up -d postgres redis kafka kafka-ui
```

### Step 3: Execute Unit & Integration Tests
```bash
# Execute full suite (33 unit & integration tests)
.\mvnw.cmd test
```

### Step 4: Run Spring Boot Application
```bash
.\mvnw.cmd spring-boot:run
```
