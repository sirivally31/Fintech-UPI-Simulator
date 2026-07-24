# Authentication & User Registration API Reference

## 1. Title & Executive Summary
**API Specification for User Onboarding, Registration, & JWT Token Emission**

This document provides comprehensive API specifications for the authentication subsystem of the UPI Simulator, detailing the endpoints for user registration (`/api/users/register`) and JWT authentication (`/api/auth/login`).

---

## 2. Why the Feature Exists
Before a user can execute transactions, create bank accounts, or link UPI VPAs, their identity must be established in the system. Registration initializes the user profile, sets their default wallet balance, and secures their account with a BCrypt-encrypted 4/6-digit PIN. Authentication verifies credentials and issues a stateless JSON Web Token (JWT) required for all protected business endpoints.

---

## 3. Enterprise Architecture & Endpoints

```
Client ──▶ POST /api/users/register ──▶ UserController ──▶ UserService ──▶ BCrypt Hash ──▶ PostgreSQL
Client ──▶ POST /api/auth/login    ──▶ AuthController ──▶ AuthenticationManager ──▶ JwtService ──▶ JWT Token
```

---

## 4. Endpoint Specifications

### 1. Register User Profile
- **Endpoint**: `POST /api/users/register`
- **Authentication**: Unauthenticated (`Public`)
- **Consumes**: `application/json`
- **Produces**: `application/json`

#### Request Body (`UserCreateDto`)
```json
{
  "name": "John Doe",
  "phoneNumber": "9876543210",
  "upiId": "john@upi",
  "pin": "1234",
  "initialBalance": 1000.00
}
```

#### Field Constraint Table
| Field Name | Type | Validation Rules | Description |
| :--- | :--- | :--- | :--- |
| `name` | String | `@NotBlank(message = "Name cannot be empty")` | Full legal name |
| `phoneNumber` | String | `@NotBlank`, `@Pattern(regexp = "^\\d{10}$")` | 10-digit mobile number |
| `upiId` | String | `@NotBlank(message = "UPI ID cannot be empty")` | Primary VPA handle |
| `pin` | String | `@NotBlank`, `@Size(min = 4, max = 6)` | 4 to 6-digit numeric PIN |
| `initialBalance` | BigDecimal | Optional | Starting account balance |

#### Response (`HTTP 201 Created`)
```json
{
  "id": 1,
  "name": "John Doe",
  "phoneNumber": "9876543210",
  "upiId": "john@upi",
  "balance": 1000.00
}
```

#### Error Responses
- **`400 Bad Request`**: Validation error (e.g., phone number not 10 digits) or duplicate UPI ID / phone number.

---

### 2. User Authentication (Login)
- **Endpoint**: `POST /api/auth/login`
- **Authentication**: Unauthenticated (`Public`)
- **Consumes**: `application/json`
- **Produces**: `application/json`

#### Request Body (`LoginRequestDto`)
```json
{
  "upiId": "john@upi",
  "pin": "1234"
}
```

#### Response (`HTTP 200 OK`)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQHVwaSIsImlhdCI6MTcyMTgyOTYwMCwiZXhwIjoxNzIxOTE2MDAwfQ.signature..."
}
```

#### Error Responses
- **`401 Unauthorized`**: Bad credentials (invalid UPI ID or incorrect PIN).

---

## 5. Important Classes Involved
- `com.example.demo.controller.UserController`
- `com.example.demo.controller.AuthController`
- `com.example.demo.service.UserService`
- `com.example.demo.security.JwtService`
- `com.example.demo.dto.UserCreateDto`
- `com.example.demo.dto.LoginRequestDto`
- `com.example.demo.dto.LoginResponseDto`

---

## 6. Security Considerations
- PIN values are hashed via `PasswordEncoder.encode()` before database insertion.
- JWT tokens expire in 24 hours (`86,400,000` ms).
- Token string must be included in subsequent requests as `Authorization: Bearer <token>`.
