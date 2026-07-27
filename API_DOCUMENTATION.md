# 🔌 API Documentation - Enterprise Fintech UPI Simulator

This document provides a comprehensive REST API reference for the Enterprise Fintech UPI Payment Simulator across Modules 1 through 15. All APIs return JSON responses using standardized response structures.

---

## 📖 Standard Response Wrapper

All REST endpoints wrap data payloads inside a unified generic response wrapper:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-07-27T22:00:00Z"
}
```

---

## 📖 Module API Summary Matrix

| Module | Base Path | Key Capabilities | Auth Required |
| :--- | :--- | :--- | :---: |
| **Module 1: Auth** | `/api/v1/auth` | User Registration, Login, Token Refresh | ❌ (Public) |
| **Module 2: Bank Accounts** | `/api/v1/accounts` | Link Bank Account, Fetch Balance, List Accounts | ✅ Bearer |
| **Module 3: UPI IDs** | `/api/v1/upi-ids` | Create VPA, Set Primary VPA, Toggle Status | ✅ Bearer |
| **Module 4: UPI PIN** | `/api/v1/upi-pin` | Set PIN, Change PIN, Reset PIN with OTP | ✅ Bearer |
| **Module 5: Transactions** | `/api/v1/transactions` | P2P Transfer, Transaction History, Download Receipt | ✅ Bearer |
| **Module 6: Payment Requests** | `/api/v1/payment-requests` | Create Collect Request, Accept Request, Decline Request | ✅ Bearer |
| **Module 7: Infrastructure** | `/actuator` | Health Checks, Prometheus Metrics, Environment Info | ❌ / Admin |
| **Module 8: Merchant System**| `/api/v1/merchants` | Merchant Registration, Static & Dynamic QR Generation | ✅ Bearer / Secret |
| **Module 9: QR Execution** | `/api/v1/qr` | Decode QR Token, Execute QR Payment | ✅ Bearer |
| **Module 10: Beneficiaries** | `/api/v1/beneficiaries` | Add Payee, Update Contact, Remove Payee | ✅ Bearer |
| **Module 11: AutoPay** | `/api/v1/autopay` | Create Mandate, Pause/Resume Mandate, Cancel Mandate | ✅ Bearer |
| **Module 12: Fraud & Risk** | `/api/v1/fraud` | Check VPA Risk Score, Update Fraud Rules | ✅ Bearer / Admin |
| **Module 13: Notifications** | `/api/v1/notifications` | User Inbox, Mark Notification as Read | ✅ Bearer |
| **Module 14: Admin Dashboard** | `/api/v1/admin` | System Metrics, Audit Logs, Merchant Approval | ✅ Admin |
| **Module 15: Settlements** | `/api/v1/settlements` | Trigger Settlement Batch, Settlement History | ✅ Admin |

---

## 1. Module 1: Authentication & JWT (`/api/v1/auth`)

### Endpoints Group
- **`POST /api/v1/auth/register`** – Register a new user account.
- **`POST /api/v1/auth/login`** – Authenticate credentials and receive a JWT Bearer token.
- **`POST /api/v1/auth/refresh`** – Obtain a fresh JWT token using a valid refresh token.

#### Sample Request (`POST /api/v1/auth/register`):
```json
{
  "username": "john_doe",
  "password": "SecurePassword123!",
  "email": "john@example.com",
  "phoneNumber": "+919876543210",
  "fullName": "John Doe"
}
```

#### Sample Response (`POST /api/v1/auth/login`):
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTY3Mj...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "username": "john_doe"
  },
  "timestamp": "2026-07-27T22:00:00Z"
}
```

---

## 2. Module 2: Bank Account Management (`/api/v1/accounts`)

### Endpoints Group
- **`POST /api/v1/accounts/link`** – Link a core bank account to the user profile.
- **`GET /api/v1/accounts`** – List all linked bank accounts for the authenticated user.
- **`GET /api/v1/accounts/{accountId}/balance`** – Query real-time balance for an account.
- **`PUT /api/v1/accounts/{accountId}/primary`** – Set account as primary funding source.

#### Sample Request (`POST /api/v1/accounts/link`):
```json
{
  "accountNumber": "112233445566",
  "ifscCode": "HDFC0001234",
  "bankName": "HDFC Bank",
  "accountType": "SAVINGS"
}
```

---

## 3. Module 3: UPI ID Management (`/api/v1/upi-ids`)

### Endpoints Group
- **`POST /api/v1/upi-ids/create`** – Register a custom Virtual Payment Address (`vpa`).
- **`GET /api/v1/upi-ids/my-vpas`** – Retrieve all VPAs owned by the authenticated user.
- **`PUT /api/v1/upi-ids/{vpaId}/primary`** – Set a VPA as primary.
- **`GET /api/v1/upi-ids/check-availability?vpa=john@upi`** – Verify if a VPA handle is available.

---

## 4. Module 4: UPI PIN Management (`/api/v1/upi-pin`)

### Endpoints Group
- **`POST /api/v1/upi-pin/set`** – Set initial 4-6 digit UPI PIN for a VPA.
- **`PUT /api/v1/upi-pin/change`** – Update existing UPI PIN by providing current PIN.
- **`POST /api/v1/upi-pin/verify`** – Validate a UPI PIN before executing operations.
- **`POST /api/v1/upi-pin/reset-request`** – Request OTP for PIN reset.
- **`POST /api/v1/upi-pin/reset-confirm`** – Confirm PIN reset using OTP.

---

## 5. Module 5: Money Transfer (`/api/v1/transactions`)

### Endpoints Group
- **`POST /api/v1/transactions/transfer`** – Execute P2P/P2M money transfer.
- **`GET /api/v1/transactions/history`** – Paginated transaction history for the user.
- **`GET /api/v1/transactions/{reference}`** – Fetch transaction detail receipt by reference.

#### Sample Request (`POST /api/v1/transactions/transfer`):
```json
{
  "senderVpa": "john@upi",
  "receiverVpa": "alice@upi",
  "amount": 1500.00,
  "upiPin": "123456",
  "remarks": "Dinner splitting"
}
```

---

## 6. Module 6: Payment Requests (`/api/v1/payment-requests`)

### Endpoints Group
- **`POST /api/v1/payment-requests/create`** – Issue a collect money request.
- **`GET /api/v1/payment-requests/pending`** – List incoming pending payment requests.
- **`POST /api/v1/payment-requests/{requestId}/accept`** – Pay collect request (requires PIN).
- **`POST /api/v1/payment-requests/{requestId}/reject`** – Decline payment request.
- **`POST /api/v1/payment-requests/{requestId}/cancel`** – Revoke outgoing collect request.

---

## 7. Module 7: Infrastructure & Actuator (`/actuator`)

### Endpoints Group
- **`GET /actuator/health`** – Query health status of DB, Redis, and Kafka brokers.
- **`GET /actuator/prometheus`** – Export Prometheus micrometer metrics.
- **`GET /actuator/info`** – System metadata and build information.

---

## 8. Module 8: Merchant QR Payment System (`/api/v1/merchants`)

### Endpoints Group
- **`POST /api/v1/merchants/register`** – Onboard business merchant account.
- **`POST /api/v1/merchants/qr/generate-static`** – Generate permanent merchant QR.
- **`POST /api/v1/merchants/qr/generate-dynamic`** – Generate time-bound dynamic QR for specific amount.

---

## 9. Module 9: QR Payment Execution (`/api/v1/qr`)

### Endpoints Group
- **`GET /api/v1/qr/decode/{qrToken}`** – Scan and parse payload details of a QR token.
- **`POST /api/v1/qr/pay`** – Execute payment against a scanned merchant QR code.

---

## 10. Module 10: Beneficiary Management (`/api/v1/beneficiaries`)

### Endpoints Group
- **`POST /api/v1/beneficiaries`** – Add trusted contact payee.
- **`GET /api/v1/beneficiaries`** – List saved beneficiaries.
- **`PUT /api/v1/beneficiaries/{id}`** – Update beneficiary contact details or nickname.
- **`DELETE /api/v1/beneficiaries/{id}`** – Remove saved payee contact.

---

## 11. Module 11: AutoPay & Scheduled Payments (`/api/v1/autopay`)

### Endpoints Group
- **`POST /api/v1/autopay/create`** – Set up recurring mandate subscription.
- **`GET /api/v1/autopay/mandates`** – View active user mandates.
- **`POST /api/v1/autopay/{mandateId}/pause`** – Pause mandate execution.
- **`POST /api/v1/autopay/{mandateId}/cancel`** – Revoke mandate subscription.

---

## 12. Module 12: Fraud Detection & Risk Engine (`/api/v1/fraud`)

### Endpoints Group
- **`GET /api/v1/fraud/risk-score?vpa=john@upi`** – Query risk score for a VPA.
- **`GET /api/v1/fraud/rules`** – List active risk engine rules.
- **`PUT /api/v1/fraud/rules/{ruleId}`** – Update risk threshold parameters (Admin).

---

## 13. Module 13: Notification Service (`/api/v1/notifications`)

### Endpoints Group
- **`GET /api/v1/notifications`** – Retrieve user notification inbox messages.
- **`PUT /api/v1/notifications/{notifId}/read`** – Mark notification as read.
- **`GET /api/v1/notifications/unread-count`** – Fetch unread message counter badge.

---

## 14. Module 14: Administrative Dashboard (`/api/v1/admin`)

### Endpoints Group
- **`GET /api/v1/admin/dashboard/summary`** – Executive overview of system volume and throughput.
- **`GET /api/v1/admin/users`** – Paginated administration of registered user accounts.
- **`POST /api/v1/admin/merchants/{merchantId}/approve`** – Approve merchant application.

---

## 15. Module 15: Settlement & Reconciliation (`/api/v1/settlements`)

### Endpoints Group
- **`POST /api/v1/settlements/trigger`** – Manually trigger merchant settlement batching.
- **`GET /api/v1/settlements/merchant/{merchantId}`** – Fetch settlement history for a merchant.
- **`GET /api/v1/settlements/reconciliation-report`** – Download daily reconciliation summary.

---
*For data schemas, consult [SYSTEM_DESIGN.md](file:///e:/UPI%20Simulator/SYSTEM_DESIGN.md).*
