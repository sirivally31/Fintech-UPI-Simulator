# Redis Caching, Distributed Locks, & Rate Limiting Architecture

## 1. Title & Executive Summary
**In-Memory Redis 7 Data Store, Redisson Distributed Synchronization, & Throttling Architecture**

This document describes how Redis 7 is integrated into the UPI Simulator to deliver sub-millisecond key-value caching, time-to-live (TTL) managed OTP storage, distributed resource locking, and API request rate limiting.

---

## 2. Why the Feature Exists
High-concurrency fintech platforms demand specialized in-memory capabilities beyond standard relational databases:
- **Concurrency & Race Condition Defense**: When multiple API requests attempt to debit or credit the same bank account simultaneously across horizontally scaled application instances, database row locks can cause deadlocks or performance bottlenecks. Distributed locks synchronize access in memory before database transactions execute.
- **Short-Lived Ephemeral State**: One-Time Passwords (OTPs) generated for registration or PIN resets must expire automatically after 5 minutes. Storing ephemeral OTPs in PostgreSQL creates disk I/O bloat and requires custom cleanup jobs.
- **DDoS & Brute-Force Rate Limiting**: Malicious actors or runaway clients submitting rapid requests to PIN verification endpoints must be throttled at the network edge before overwhelming backend compute.

---

## 3. Enterprise Architecture (Redis Subsystems)

```
                              ┌────────────────────────┐
                              │  Spring Boot App Node  │
                              └───────────┬────────────┘
                                          │
        ┌─────────────────────────────────┼─────────────────────────────────┐
        │                                 │                                 │
        ▼                                 ▼                                 ▼
┌──────────────────────┐      ┌──────────────────────┐      ┌──────────────────────┐
│  RedisCacheService   │      │   OtpCacheService    │      │ DistributedLockSvc   │
│ (Generic Operations) │      │  (TTL Managed OTP)   │      │ (Redisson / Atomic)  │
└──────────┬───────────┘      └──────────┬───────────┘      └──────────┬───────────┘
           │                             │                             │
           └─────────────────────────────┼─────────────────────────────┘
                                         │ (TCP Port 6379)
                                         ▼
                              ┌────────────────────────┐
                              │     Redis 7 Server     │
                              │  (In-Memory Key/Value) │
                              └────────────────────────┘
```

---

## 4. How Our Implementation Works

### 1. Ephemeral OTP Cache (`OtpCacheServiceImpl`)
- Generates 6-digit numeric OTP strings.
- Stores OTPs using namespace `OTP:<phoneNumber>` (e.g. `OTP:9876543210`).
- Configures explicit 5-minute TTL: `redisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES)`.
- When `validateOtp(phoneNumber, inputOtp)` is called:
  - Fetches cached value.
  - Returns `true` if input matches cached string.
  - Immediately deletes key upon successful validation to prevent single-use replay attacks.

### 2. Distributed Locks (`DistributedLockServiceImpl`)
- Provides mutual exclusion for financial account updates across clustered nodes.
- Uses Redisson / Redis atomic commands (`SET LOCK:ACCOUNT:<id> <uuid> NX PX <timeout>`).
- Method signature: `acquireLock(String lockKey, long waitTime, long leaseTime)`.
- If Node A holds `LOCK:ACCOUNT:10`, Node B's request waits up to `waitTime` milliseconds before failing fast or retrying.
- Guarantees lock release in a `finally` block via `releaseLock(lockKey)`.

### 3. API Rate Limiter (`RateLimiterServiceImpl`)
- Tracks client request frequencies per sliding or fixed window using Redis key `RATELIMIT:<clientId>:<endpoint>`.
- Increments counter using `redisTemplate.opsForValue().increment(key)`.
- If counter == 1, sets expiration window (e.g., 60 seconds).
- If counter exceeds max allowed requests (e.g. 10 requests/minute), service throws `RateLimitExceededException` (HTTP 429 Too Many Requests).

---

## 5. Request Lifecycle (Redis Lock & OTP Verification)

```
Client ──▶ Request Money Transfer
              │
              ▼
    DistributedLockService.acquireLock("LOCK:ACCOUNT:10")
              │
         Acquired? ──[NO]──▶ Return HTTP 429 / 503 Retry
              │ [YES]
              ▼
    Execute Financial Debit/Credit Logic (@Transactional)
              │
              ▼
    DistributedLockService.releaseLock("LOCK:ACCOUNT:10")
              │
              ▼
    Client ◄── Receive Success Response
```

---

## 6. Redis Key Schema & TTL Policy

| Key Pattern | Data Type | Expiration (TTL) | Purpose |
| :--- | :--- | :--- | :--- |
| `OTP:<phoneNumber>` | `String` | 5 Minutes (300s) | Single-use One-Time Password storage |
| `LOCK:ACCOUNT:<accountId>` | `String` | 10 Seconds (Lease) | Distributed mutex lock for account transactions |
| `RATELIMIT:<clientId>:<endpoint>` | `Integer` | 60 Seconds | Sliding window API invocation counter |
| `CACHE:USER:<upiId>` | `JSON Object`| 15 Minutes | Cached user profile metadata |

---

## 7. Spring Boot Components Involved

- `org.springframework.data.redis.core.RedisTemplate`: Primary Spring Redis client abstraction.
- `com.example.demo.service.RedisCacheService` & `RedisCacheServiceImpl`: Core key-value wrapper.
- `com.example.demo.service.OtpCacheService` & `OtpCacheServiceImpl`: Ephemeral OTP management.
- `com.example.demo.service.DistributedLockService` & `DistributedLockServiceImpl`: Lock manager.
- `com.example.demo.service.RateLimiterService` & `RateLimiterServiceImpl`: Request throttling service.

---

## 8. Security & Resilience Considerations

- **Redis Connection Failures**: Configured with automatic reconnection attempts and 2000ms timeout bounds (`spring.data.redis.timeout=2000`).
- **Lock Deadlock Defense**: Locks always specify lease expiration times so that crashed nodes do not leave orphaned locks permanently blocking account processing.
- **Single-Use OTP Invalidation**: Keys are deleted immediately upon validation.

---

## 9. Future Improvements

- **Redis Sentinel / Cluster Deployment**: Multi-master or primary-replica Redis cluster setup for high-availability failover.
- **Lua Scripting for Atomic Rate Limiting**: Executing sliding-window rate limit checks inside atomic Redis Lua scripts to eliminate race conditions between `INCR` and `EXPIRE`.
