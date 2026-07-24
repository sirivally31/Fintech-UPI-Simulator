# UPI ID (Virtual Payment Address) API Reference

## 1. Title & Executive Summary
**API Specification for VPA Management, Handle Generation, & Primary Account Assignment**

This document specifies API endpoints governing Virtual Payment Address (UPI ID) handles, handle generation (`preferredHandle`), primary VPA assignment, and VPA status management.

---

## 2. Why the Feature Exists
UPI hides complex 10-to-16 digit bank account numbers and IFSC codes behind human-readable Virtual Payment Addresses (e.g. `john@oksbi`, `alice@okicici`). This API enables creating secondary VPAs and binding them to linked bank accounts.

---

## 3. Endpoint Specifications

### 1. Create UPI ID
- **Endpoint**: `POST /api/upi`
- **Authentication**: Required (`Bearer <token>`)

#### Request Body (`CreateUpiIdRequest`)
```json
{
  "bankAccountId": 10,
  "preferredHandle": "oksbi"
}
```

#### Field Validation Rules
| Field | Type | Rules | Description |
| :--- | :--- | :--- | :--- |
| `bankAccountId` | Long | `@NotNull` | Target bank account ID |
| `preferredHandle` | String | `@NotBlank`, `@Pattern("^[a-zA-Z0-9]+$")` | Preferred handle string (e.g. `oksbi`, `paytm`) |

#### Response (`HTTP 201 Created`)
```json
{
  "id": 100,
  "upiId": "john.doe@oksbi",
  "status": "ACTIVE",
  "isPrimary": true,
  "bankAccountNumber": "1048291034",
  "createdAt": "2026-07-24T15:35:00"
}
```

---

### 2. Get All User UPI IDs
- **Endpoint**: `GET /api/upi`
- **Authentication**: Required (`Bearer <token>`)

#### Response (`HTTP 200 OK`)
```json
[
  {
    "id": 100,
    "upiId": "john.doe@oksbi",
    "status": "ACTIVE",
    "isPrimary": true,
    "bankAccountNumber": "1048291034"
  }
]
```

---

### 3. Set Primary UPI ID
- **Endpoint**: `PUT /api/upi/{id}/primary`
- **Authentication**: Required (`Bearer <token>`)

#### Response (`HTTP 200 OK`)
```json
{
  "id": 100,
  "upiId": "john.doe@oksbi",
  "status": "ACTIVE",
  "isPrimary": true,
  "bankAccountNumber": "1048291034"
}
```

---

## 4. Important Classes Involved
- `com.example.demo.controller.UpiIdController`
- `com.example.demo.service.UpiIdService` & `UpiIdServiceImpl`
- `com.example.demo.repository.UpiIdRepository`
- `com.example.demo.dto.CreateUpiIdRequest`
- `com.example.demo.dto.UpiIdResponse`
