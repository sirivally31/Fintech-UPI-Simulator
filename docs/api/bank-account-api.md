# Bank Account Management API Reference

## 1. Title & Executive Summary
**API Specification for Bank Account Linkage, Querying, Updating, & Deletion**

This document provides technical API specifications for managing bank accounts linked to an authenticated user profile in the UPI Simulator.

---

## 2. Why the Feature Exists
In real-world UPI, users link one or more commercial bank accounts (HDFC, ICICI, SBI) to their mobile number profile. These bank accounts act as the actual monetary source and destination for all Peer-to-Peer (P2P) transfers and collect requests.

---

## 3. Endpoint Specifications

### 1. Create Bank Account
- **Endpoint**: `POST /api/accounts`
- **Authentication**: Required (`Bearer <token>`)

#### Request Body (`CreateBankAccountRequest`)
```json
{
  "bankName": "HDFC Bank",
  "ifscCode": "HDFC0001234",
  "accountType": "SAVINGS",
  "balance": 2500.00
}
```

#### Field Validation Rules
| Field | Type | Rules | Description |
| :--- | :--- | :--- | :--- |
| `bankName` | String | `@NotBlank` | Commercial bank name |
| `ifscCode` | String | `@NotBlank`, `@Size(min=11, max=11)` | 11-character IFSC code |
| `accountType` | Enum | `@NotNull` | `SAVINGS` or `CURRENT` |
| `balance` | BigDecimal | Optional | Initial deposit balance |

#### Response (`HTTP 201 Created`)
```json
{
  "id": 10,
  "accountNumber": "1048291034",
  "bankName": "HDFC Bank",
  "ifscCode": "HDFC0001234",
  "accountType": "SAVINGS",
  "balance": 2500.00,
  "status": "ACTIVE",
  "createdAt": "2026-07-24T15:30:00"
}
```

---

### 2. Get All Linked Bank Accounts
- **Endpoint**: `GET /api/accounts`
- **Authentication**: Required (`Bearer <token>`)

#### Response (`HTTP 200 OK`)
```json
[
  {
    "id": 10,
    "accountNumber": "1048291034",
    "bankName": "HDFC Bank",
    "ifscCode": "HDFC0001234",
    "accountType": "SAVINGS",
    "balance": 2500.00,
    "status": "ACTIVE"
  }
]
```

---

### 3. Get Bank Account By ID
- **Endpoint**: `GET /api/accounts/{id}`
- **Authentication**: Required (`Bearer <token>`)

#### Response (`HTTP 200 OK`)
```json
{
  "id": 10,
  "accountNumber": "1048291034",
  "bankName": "HDFC Bank",
  "ifscCode": "HDFC0001234",
  "accountType": "SAVINGS",
  "balance": 2500.00,
  "status": "ACTIVE"
}
```

---

### 4. Delete Bank Account
- **Endpoint**: `DELETE /api/accounts/{id}`
- **Authentication**: Required (`Bearer <token>`)

#### Response (`HTTP 204 No Content`)

---

## 4. Important Classes Involved
- `com.example.demo.controller.BankAccountController`
- `com.example.demo.service.BankAccountService` & `BankAccountServiceImpl`
- `com.example.demo.repository.BankAccountRepository`
- `com.example.demo.dto.CreateBankAccountRequest`
- `com.example.demo.dto.BankAccountResponse`
