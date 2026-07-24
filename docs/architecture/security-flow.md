# Cryptographic Security & Authorization Architecture

## 1. Title & Executive Summary
**Stateless Security Filter Chain, JWT Bearer Token Validation, & BCrypt PIN Protection**

This document details the security architecture of the UPI Payment Simulator. It describes the stateless authentication pipeline, cryptographic hashing standards, JWT token issuance and validation, SecurityContext management, and resource ownership enforcement (IDOR protection).

---

## 2. Why the Feature Exists
Financial API security must protect against severe vulnerability vectors:
- **Credential Leakage in Database**: Storing plaintext PINs or passwords poses catastrophic risk if database backups leak. Credentials must be protected using one-way cryptographic hashing algorithms with adaptive salting.
- **Session Hijacking & CSRF Attacks**: Stateful HTTP sessions require server memory and expose APIs to Cross-Site Request Forgery (CSRF). A stateless JWT architecture eliminates session state and CSRF vulnerabilities.
- **Insecure Direct Object Reference (IDOR)**: Malicious users altering request parameters (e.g. `senderBankAccountId=10`) must be blocked unless ownership of account `10` is verified against the authenticated token principal.

---

## 3. Enterprise Security Architecture

```
[Incoming HTTP Request]
          │
          ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        SPRING SECURITY FILTER CHAIN                    │
│                                                                        │
│ 1. Disable CSRF (Stateless API)                                        │
│ 2. Match Path Permitting Rules:                                        │
│    - /api/users/register ──▶ PERMIT ALL                                │
│    - /api/auth/login     ──▶ PERMIT ALL                                │
│    - /actuator/health    ──▶ PERMIT ALL                                │
│    - All Other Paths     ──▶ AUTHENTICATED                             │
│ 3. Execute JwtAuthenticationFilter                                     │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     JWT AUTHENTICATION FILTER                          │
│                                                                        │
│ 4. Read Header "Authorization: Bearer <token>"                         │
│ 5. Validate HMAC-SHA256 Signature via JwtService                       │
│ 6. Extract Subject (UPI ID) & Verify Expiration                        │
│ 7. Load UserDetails via CustomUserDetailsService                       │
│ 8. Inject Token into SecurityContextHolder.getContext().setAuth(...)   │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     SERVICE RESOURCE OWNERSHIP                         │
│                                                                        │
│ 9. Execute findByIdAndUser(accountId, currentUser)                      │
│    - Asserts account ownership before executing debits/credits         │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. How Our Implementation Works

### 1. Stateless Security Filter Chain (`SecurityConfig`)
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/users/register", "/api/auth/login", "/actuator/health", "/actuator/info", "/actuator/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### 2. JWT Generation & Signature (`JwtService`)
- Uses `io.jsonwebtoken` (JJWT 0.11.5).
- Signs tokens using HMAC-SHA256 (`SignatureAlgorithm.HS256`) and a Base64-encoded secret key (`jwt.secret`).
- Sets token subject (`userDetails.getUsername()`), issue timestamp (`issuedAt`), and expiration timestamp (`expiration = 24 hours`).

### 3. BCrypt Hashing (`PasswordEncoder` / `UpiPinServiceImpl`)
- Uses Spring Security `BCryptPasswordEncoder`.
- Automatically generates a 128-bit random salt per hash.
- Identical PINs (e.g. `"1234"`) generate different hash strings, neutralizing rainbow-table attacks.
- PIN validation relies on `passwordEncoder.matches(rawPin, storedHash)`.

---

## 5. Security Validation Matrix

| Security Vector | Implementation Mechanism | Location |
| :--- | :--- | :--- |
| **Authentication** | JWT Bearer Token validation | `JwtAuthenticationFilter` |
| **PIN Encryption** | BCrypt Adaptive Hashing | `UpiPinServiceImpl` / `UserService` |
| **IDOR Defense** | Ownership query assertion (`findByIdAndUser`) | Service Layer (`TransactionServiceImpl`) |
| **API Throttling** | Redis-backed Request Counter | `RateLimiterServiceImpl` |
| **Container Security** | Non-root `appuser` execution context | `Dockerfile` (`USER appuser`) |

---

## 6. Spring Boot Components Involved

- `com.example.demo.security.SecurityConfig`: Primary security filter chain bean definition.
- `com.example.demo.security.JwtAuthenticationFilter`: Request interceptor filter.
- `com.example.demo.security.JwtService`: JJWT utility class.
- `com.example.demo.security.CustomUserDetailsService`: Bridges database users with Spring Security `UserDetails`.

---

## 7. Future Security Enhancements

- **Key Rotation Architecture**: Rotating Base64 JWT signing keys dynamically via AWS Secrets Manager or HashiCorp Vault.
- **Biometric Token Assertions**: Supporting WebAuthn / FIDO2 signatures for mobile UPI client authorization.
