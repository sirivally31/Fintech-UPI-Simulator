# Detailed Backend Request Flow Architecture

## 1. Title & Executive Summary
**End-to-End HTTP Pipeline, Middleware Interceptors, & Processing Execution Flow**

This document presents a comprehensive analysis of the internal mechanics of the UPI Simulator backend during HTTP request processing. It traces the lifecycle of an incoming network packet from Tomcat servlet thread allocation down to SQL query generation, transaction isolation, outbox persistence, and HTTP response rendering.

---

## 2. Why the Feature Exists
In high-concurrency enterprise applications, request processing must follow a strict, predictable pipeline:
- **Middleware Standardization**: Cross-cutting concerns (authentication, authorization, logging, CORS, metric collection) must be enforced *before* requests hit business logic.
- **Fail-Fast Validation**: Malformed JSON payloads or invalid DTO fields must be rejected immediately at the API edge to preserve service compute and database connection resources.
- **Centralized Exception Transformation**: Technical stack trace leakage (e.g. SQL syntax errors or internal NullPointerExceptions) presents security vulnerabilities. A centralized error pipeline transforms internal failures into clean, RFC-compliant JSON responses.

---

## 3. Enterprise Architecture & Layer Pipeline

The request processing pipeline is divided into 5 execution stages:

```
[HTTP Request Packet]
         │
         ▼
STAGE 1: SERVLET CONTAINER (Tomcat Thread Pool)
         │
         ▼
STAGE 2: FILTER CHAIN (JwtAuthenticationFilter & SecurityFilterChain)
         │
         ▼
STAGE 3: SPRING MVC DISPATCHER (DispatcherServlet & DTO Validation)
         │
         ▼
STAGE 4: SERVICE TRANSACTION BOUNDARY (@Transactional & Hibernate ORM)
         │
         ▼
STAGE 5: EXCEPTION / RESPONSE RENDERING (GlobalExceptionHandler & Jackson)
```

---

## 4. How Our Implementation Works

### Stage 1: Servlet Container & Thread Management
- Embedded Apache Tomcat listens on port `8080`.
- Incoming TCP requests are assigned a worker thread from the Tomcat thread pool (`server.tomcat.threads.max=200`).
- The request object is wrapped in standard `HttpServletRequest` and `HttpServletResponse` containers.

### Stage 2: Security Filter Interception (`JwtAuthenticationFilter`)
- Extends Spring's `OncePerRequestFilter` to guarantee execution exactly once per request.
- Reads `Authorization` header. If absent or does not begin with `Bearer `, filter delegates down the chain.
- If present, extracts JWT string, validates HMAC-SHA256 signature using `JwtService`, and extracts the `username` (UPI ID or Phone Number).
- Invokes `CustomUserDetailsService.loadUserByUsername()` to load user authorities.
- Instantiates `UsernamePasswordAuthenticationToken` and injects it into `SecurityContextHolder.getContext().setAuthentication(...)`.

### Stage 3: MVC Dispatcher & Handler Mapping
- `DispatcherServlet` receives the request and consults `HandlerMapping` to locate the target `@RestController` and method handler.
- **Validation Processing**: Spring's `DataBinder` evaluates Jakarta Bean Validation annotations (`@Valid`). If constraints fail:
  - Intercepts execution before controller method invocation.
  - Throws `MethodArgumentNotValidException`.

### Stage 4: Service Execution & Transaction Boundary
- Controller delegates parameters to service implementation (e.g. `TransactionServiceImpl`).
- `@Transactional` annotation activates Spring's `TransactionInterceptor`:
  - Obtains a JDBC `Connection` from `HikariDataSource`.
  - Sets auto-commit to `false`.
  - Binds connection to current thread context (`TransactionSynchronizationManager`).
- Executes domain queries, updates entity state, and persists outbox events.

### Stage 5: Commit & Response Marshalling
- Service completes without exception:
  - Spring's transaction manager commits the DB transaction.
  - Connection is returned to Hikari pool.
- Jackson `ObjectMapper` serializes return object into JSON bytes.
- Controller wraps payload in `ResponseEntity.ok(...)` or `ResponseEntity.created(...)` with HTTP headers.

---

## 5. Detailed Request Lifecycle Sequence

```
1. Client ──▶ HTTP POST /api/transactions/send (Header: Authorization: Bearer <jwt>)
2. Tomcat ──▶ Allocates HTTP Worker Thread
3. JwtAuthFilter ──▶ Intercepts request
4. JwtService ──▶ Validates signature & expiration
5. SecurityContext ──▶ Populates Authentication object
6. DispatcherServlet ──▶ Resolves TransactionController.sendMoney()
7. Validator ──▶ Validates SendMoneyRequest (@NotNull, @DecimalMin)
8. TransactionController ──▶ Invokes TransactionServiceImpl.sendMoney()
9. TransactionInterceptor ──▶ Begins Database Transaction (Hikari Connection)
10. TransactionServiceImpl ──▶ Verifies account ownership, status, PIN & balance
11. Hibernate ──▶ Generates SQL UPDATE & INSERT statements
12. OutboxService ──▶ Writes OutboxEvent record to DB
13. TransactionInterceptor ──▶ Commits DB Transaction
14. Jackson ──▶ Serializes TransactionResponse DTO to JSON
15. Client ◄── Receives 200 OK + Transaction JSON
```

---

## 6. Database Interaction & Error Handling

### Exception Flow Architecture (`GlobalExceptionHandler`)
When a domain exception occurs during Stage 4:
- `@Transactional` intercepts the exception and issues a database `ROLLBACK`.
- Spring MVC routes the exception to `com.example.demo.exception.GlobalExceptionHandler` (`@RestControllerAdvice`).
- Exception handlers map domain exceptions to appropriate HTTP status codes:

| Exception Class | Mapped HTTP Status | Cause |
| :--- | :--- | :--- |
| `UnauthorizedAccountAccessException` | `401 Unauthorized` / `403 Forbidden` | User attempted to access account owned by another user |
| `BankAccountNotFoundException` / `UserNotFoundException` | `404 Not Found` | Requested entity ID does not exist |
| `SecurityException` | `400 Bad Request` | Invalid UPI PIN entered |
| `IllegalStateException` | `400 Bad Request` | Insufficient balance or inactive account/VPA |
| `MethodArgumentNotValidException` | `400 Bad Request` | Validation failure in DTO fields |

---

## 7. Spring Boot Components Involved

- `org.springframework.web.filter.OncePerRequestFilter`: Base class for `JwtAuthenticationFilter`.
- `org.springframework.security.core.context.SecurityContextHolder`: ThreadLocal storage for user authentication.
- `org.springframework.web.servlet.DispatcherServlet`: Core MVC routing engine.
- `org.springframework.transaction.interceptor.TransactionInterceptor`: AOP proxy handling transaction start/commit/rollback.
- `org.springframework.web.bind.annotation.RestControllerAdvice`: Centralized exception interceptor.

---

## 8. Security Considerations

- **ThreadLocal Cleanup**: Spring Security automatically clears `SecurityContextHolder` at thread completion, preventing user context leaks across Tomcat thread reuses.
- **OWASP Information Exposure Defense**: Stack traces are logged internally at `ERROR` level but stripped from HTTP responses.

---

## 9. Future Improvements

- **WebFlux Reactive Pipeline**: Migrating high-volume query endpoints to Non-blocking Reactive I/O (Spring WebFlux + R2DBC).
- **Global Rate Limiting Filter**: Moving Redis rate limiting into a Servlet Filter executing prior to Spring Security.
