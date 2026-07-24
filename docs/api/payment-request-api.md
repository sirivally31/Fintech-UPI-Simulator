# Payment Request (Collect) API Reference

## 1. Title & Executive Summary
**API Specification for UPI Collect Requests & Response Workflows**

This document specifies REST API endpoints managing Request for Money (Collect Request) workflows, enabling payees to request funds from payers and allowing payers to accept, reject, or cancel collect requests.

---

## 2. Why the Feature Exists
Collect payment requests (as seen in Google Pay, PhonePe, and BHIM) enable merchants and individuals to request money from a specific VPA. The payer receives a push notification and can authorize the debit using their UPI PIN.

---

## 3. Endpoint Specifications

### 1. Create Collect Request
- **Endpoint**: `POST /api/payment-requests/`
- **Authentication**: Required (`Bearer <token>`)

#### Request Body (`CreatePaymentRequestRequest`)
```json
{
  "receiverUpiId": "payer@upi",
  "amount": 150.00,
  "note": "Lunch share"
}
```

#### Response (`HTTP 201 Created`)
```json
{
  "id": 5,
  "requestReference": "REQ202607241540000001",
  "senderUpiId": "payer@upi",
  "receiverUpiId": "requester@upi",
  "amount": 150.00,
  "note": "Lunch share",
  "status": "PENDING",
  "createdAt": "2026-07-24T15:40:00",
  "expiresAt": "2026-07-25T15:40:00"
}
```

---

### 2. Accept Collect Request
- **Endpoint**: `PUT /api/payment-requests/{reference}/accept`
- **Authentication**: Required (`Bearer <token>` of Payer)

#### Request Body (`AcceptPaymentRequestRequest`)
```json
{
  "upiPin": "1234"
}
```

#### Response (`HTTP 200 OK`)
```json
{
  "id": 5,
  "requestReference": "REQ202607241540000001",
  "status": "ACCEPTED",
  "respondedAt": "2026-07-24T15:42:00"
}
```

---

### 3. Reject Collect Request
- **Endpoint**: `PUT /api/payment-requests/{reference}/reject`
- **Authentication**: Required (`Bearer <token>` of Payer)

#### Request Body (`RejectPaymentRequestRequest`)
```json
{
  "reason": "Unrecognized request"
}
```

#### Response (`HTTP 200 OK`)
```json
{
  "id": 5,
  "requestReference": "REQ202607241540000001",
  "status": "REJECTED",
  "respondedAt": "2026-07-24T15:43:00"
}
```

---

### 4. Cancel Collect Request
- **Endpoint**: `PUT /api/payment-requests/{reference}/cancel`
- **Authentication**: Required (`Bearer <token>` of Requester)

#### Request Body (`CancelPaymentRequestRequest`)
```json
{}
```

#### Response (`HTTP 200 OK`)
```json
{
  "id": 5,
  "requestReference": "REQ202607241540000001",
  "status": "CANCELLED"
}
```

---

## 4. Important Classes Involved
- `com.example.demo.controller.PaymentRequestController`
- `com.example.demo.service.impl.PaymentRequestServiceImpl`
- `com.example.demo.repository.PaymentRequestRepository`
- `com.example.demo.entity.PaymentRequest`
- `com.example.demo.entity.PaymentRequestStatus`
