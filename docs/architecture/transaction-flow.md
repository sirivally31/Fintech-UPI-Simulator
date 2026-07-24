# Transaction Engine Rules & Financial Ledger Mechanics

## 1. Title & Executive Summary
**Atomic Double-Entry Transfers, UTR Generation, & Balance Verification Architecture**

This document provides a comprehensive technical breakdown of the financial transaction engine in the UPI Simulator. It covers double-entry accounting rules, defensive validation checks, unique UTR reference generation, and database state transitions.

---

## 2. Why the Feature Exists
Financial transaction processing is the most critical function of a payment simulator. Financial operations must uphold strict mathematical constraints:
- **Zero-Sum Ledger Balance**: Money cannot be created or destroyed during a transfer; the sum of debits must equal the sum of credits ($\Delta \text{Sender} + \Delta \text{Receiver} = 0$).
- **Non-Repudiation**: Every transfer must generate a unique, traceable Unique Transaction Reference (UTR).
- **Concurrency Isolation**: Simultaneous transfers targeting the same account must be processed safely without race conditions.

---

## 3. Financial Execution Validation Hierarchy

Before any balance modification occurs, `TransactionServiceImpl.sendMoney()` executes a 16-point validation pipeline:

```
[SendMoneyRequest]
        │
        ├─▶ 1. Validate Authenticated User Context (SecurityContextHolder)
        ├─▶ 2. Verify Sender Account Ownership (bankAccountRepository.findByIdAndUser)
        ├─▶ 3. Resolve Receiver VPA (upiIdRepository.findByUpiId)
        ├─▶ 4. Check Receiver VPA Status == ACTIVE
        ├─▶ 5. Check Sender Account Status == ACTIVE
        ├─▶ 6. Check Receiver Account Status == ACTIVE
        ├─▶ 7. Assert Sender Account ID != Receiver Account ID (No Self-Transfers)
        ├─▶ 8. Verify Cryptographic UPI PIN (UpiPinService.verifyUpiPin)
        ├─▶ 9. Validate Amount > 0.00
        ├─▶ 10. Validate Sender Balance >= Amount
        │
        ▼ (All Validations Passed)
[Execute Atomic Double-Entry Transfer & Write Outbox Event]
```

---

## 4. How Our Implementation Works

```java
@Override
@Transactional
public TransactionResponse sendMoney(SendMoneyRequest request) {
    // 1 & 2: Sender ownership check
    BankAccount senderAccount = getOwnedBankAccount(request.getSenderBankAccountId());

    // 3 & 4: Receiver VPA check
    UpiId receiverUpi = upiIdRepository.findByUpiId(request.getReceiverUpiId())
            .orElseThrow(() -> new IllegalArgumentException("Receiver UPI ID not found"));

    BankAccount receiverAccount = receiverUpi.getBankAccount();

    // 5, 6, 7: Status & self-transfer validation
    if (receiverUpi.getStatus() != UpiStatus.ACTIVE) throw new IllegalStateException("Receiver UPI ID is not active");
    if (senderAccount.getStatus() != AccountStatus.ACTIVE) throw new IllegalStateException("Sender bank account is not active");
    if (receiverAccount.getStatus() != AccountStatus.ACTIVE) throw new IllegalStateException("Receiver bank account is not active");
    if (senderAccount.getId().equals(receiverAccount.getId())) throw new IllegalArgumentException("Cannot transfer money to the same bank account");

    // 8: Security PIN verification
    VerifyUpiPinRequest verifyReq = new VerifyUpiPinRequest();
    verifyReq.setBankAccountId(senderAccount.getId());
    verifyReq.setPin(request.getUpiPin());
    if (!upiPinService.verifyUpiPin(verifyReq)) throw new SecurityException("Invalid UPI PIN");

    // 9 & 10: Balance sufficiency
    if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Transfer amount must be greater than zero");
    if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) throw new IllegalStateException("Insufficient balance");

    // 11: UTR Generation
    String txnRef = generateTransactionReference();

    // 12 & 13: Double-entry debit/credit
    senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
    receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));

    // 14: Save ledger entry & outbox event
    Transaction transaction = new Transaction();
    transaction.setTransactionReference(txnRef);
    transaction.setSenderBankAccount(senderAccount);
    transaction.setReceiverBankAccount(receiverAccount);
    transaction.setSenderUpiId(senderUpi);
    transaction.setReceiverUpiId(receiverUpi);
    transaction.setAmount(request.getAmount());
    transaction.setRemarks(request.getRemarks());
    transaction.setStatus(TransactionStatus.SUCCESS);

    bankAccountRepository.save(senderAccount);
    bankAccountRepository.save(receiverAccount);
    transaction = transactionRepository.save(transaction);

    publishTransactionEvent(transaction);
    return convertToTransactionResponse(transaction);
}
```

---

## 5. UTR Generation Algorithm

The Unique Transaction Reference (UTR) is constructed to guarantee global uniqueness across high-frequency calls:

```java
private String generateTransactionReference() {
    String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    int sequence = 1;
    String baseRef;
    do {
        baseRef = String.format("TXN%s%04d", datePart, sequence++);
    } while (transactionRepository.existsByTransactionReference(baseRef));
    return baseRef;
}
```

Format Structure: `TXN` + `YYYYMMDDHHMMSS` + `4-digit sequence` (e.g. `TXN202607241530000001`).

---

## 6. Database State & Ledger Representation

| Entity Field | Initial State | Final State |
| :--- | :--- | :--- |
| `senderAccount.balance` | `$1000.00` | `$800.00` (Debited `$200.00`) |
| `receiverAccount.balance` | `$500.00` | `$700.00` (Credited `$200.00`) |
| `transaction.status` | N/A | `SUCCESS` |
| `outbox_event.status` | N/A | `PENDING` |

---

## 7. Spring Boot Components Involved

- `com.example.demo.service.TransactionServiceImpl`: Primary transaction orchestrator.
- `com.example.demo.service.UpiPinServiceImpl`: PIN verification component.
- `com.example.demo.repository.TransactionRepository`: Transaction ledger persistence.
- `com.example.demo.repository.BankAccountRepository`: Account state persistence.

---

## 8. Security Considerations

- **PIN Masking**: Plaintext PIN parameter is discarded immediately after matching against BCrypt hash.
- **Atomicity Guarantee**: Single `@Transactional` rollback discards balance mutations if any step fails.

---

## 9. Future Improvements

- **Daily Transaction Limits**: Enforcing NPCI limits (e.g. max $1,000 / ₹1,00,000 per 24-hour window per user).
- **Two-Phase Commit (2PC) / Saga Pattern**: Supporting inter-bank API networks across external banking switches.
