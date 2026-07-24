# Transaction API Reference

## 1. Title & Executive Summary
**API Specification for Real-Time P2P Money Transfers, History Retrieval, & Summaries**

This document specifies REST API endpoints handling Peer-to-Peer (P2P) funds transfer, unique transaction reference (UTR) lookup, transaction history listing, and aggregated user summaries.

---

## 2. Why the Feature Exists
Money transfer is the core value proposition of the UPI ecosystem. It allows instantaneous, 24/7/365 funds movement between accounts without entering beneficiary account numbers or waiting for settlement windows.

---

## 3. Endpoint Specifications

### 1. Send Money (P2P Transfer)
- **Endpoint**: `POST /api/transactions/send`
- **Authentication**: Required (`Bearer <token>`)

#### Request Body (`SendMoneyRequest`)
```json
{
  "senderBankAccountId": 10,
  "receiverUpiId": "alice@upi",
  "amount": 200.00,
  "upiPin": "1234",
  "remarks": "Dinner split"
}
```

#### Field Validation Rules
| Field | Type | Rules | Description |
| :--- | :--- | :--- | :--- |
| `senderBankAccountId` | Long | `@NotNull` | Sender bank account ID |
| `receiverUpiId` | String | `@NotBlank` | Receiver VPA handle |
| `amount` | BigDecimal | `@NotNull`, `@DecimalMin("0.01")` | Transfer amount (> 0.00) |
| `upiPin` | String | `@NotBlank` | 4/6-digit numeric UPI PIN |
| `remarks` | String | Optional | Optional transaction note |

#### Response (`HTTP 200 OK`)
```json
{
  "transactionReference": "TXN202607241535000001",
  "senderUpiId": "john@upi",
  "receiverUpiId": "alice@upi",
  "amount": 200.00,
  "remarks": "Dinner split",
  "status": "SUCCESS",
  "createdAt": "2026-07-24T15:35:01"
}
```

#### Common Error Codes
- **`400 Bad Request`**: Invalid PIN, insufficient balance, or self-transfer attempt.
- **`404 Not Found`**: Target receiver UPI ID not found.

---

### 2. Get Transaction History
- **Endpoint**: `GET /api/transactions/history`
- **Authentication**: Required (`Bearer <token>`)

#### Response (`HTTP 200 OK`)
```json
[
  {
    "transactionReference": "TXN202607241535000001",
    "senderUpiId": "john@upi",
    "receiverUpiId": "alice@upi",
    "amount": 200.00,
    "status": "SUCCESS",
    "remarks": "Dinner split",
    "createdAt": "2026-07-24T15:35:01"
  }
]
```

---

### 3. Get Transaction Summary
- **Endpoint**: `GET /api/transactions/summary`
- **Authentication**: Required (`Bearer <token>`)

#### Response (`HTTP 200 OK`)
```json
{
  "totalSent": 200.00,
  "totalReceived": 500.00,
  "totalTransactions": 3
}
```

---

## 4. Important Classes Involved
- `com.example.demo.controller.TransactionController`
- `com.example.demo.service.TransactionService` & `TransactionServiceImpl`
- `com.example.demo.repository.TransactionRepository`
- `com.example.demo.dto.SendMoneyRequest`
- `com.example.demo.dto.TransactionResponse`
- `com.example.demo.dto.TransactionSummaryResponse`
