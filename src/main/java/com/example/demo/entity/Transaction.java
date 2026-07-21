package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a financial transaction between two UPI IDs and their respective Bank Accounts.
 * 
 * <h3>Architecture and Financial Systems Design:</h3>
 * 
 * <p><b>Why money should use BigDecimal instead of double:</b></p>
 * <p>Never use floating-point types (like {@code float} or {@code double}) for representing currency.
 * Floating-point arithmetic introduces rounding errors due to how base-2 fractions are represented in memory
 * (e.g., 0.1 + 0.2 can result in 0.30000000000000004). {@code BigDecimal} represents numbers using base-10
 * and provides complete control over precision and rounding, ensuring accurate financial calculations.</p>
 * 
 * <p><b>Why transaction records should never be deleted:</b></p>
 * <p>Financial systems operate on an append-only ledger model. Deleting records destroys the audit trail.
 * If a transaction is reversed or fails, we insert a new record or update its status, but we never remove 
 * historical data. This is a strict requirement for regulatory compliance, fraud investigation, and reconciliation.</p>
 * 
 * <p><b>Why transaction references must be unique:</b></p>
 * <p>A unique transaction reference (like a UTR number in Indian banking) ensures idempotency and traceability.
 * Idempotency means if a client retries the exact same request due to a network timeout, the system can use 
 * the unique reference to realize it already processed that transaction, preventing double-charging.</p>
 * 
 * <p><b>Why ManyToOne relationships are appropriate:</b></p>
 * <p>In this system, a single BankAccount or UpiId can participate in many transactions (as a sender or receiver). 
 * The {@code @ManyToOne} annotation models this correctly from the perspective of the Transaction. It stores the 
 * foreign key of the BankAccount/UpiId in the transactions table, normalizing the data and ensuring referential integrity.</p>
 * 
 * <p><b>How @Transactional will later guarantee atomic transfers:</b></p>
 * <p>When transferring funds, we must deduct from the sender, add to the receiver, and save this Transaction record.
 * If any of these steps fail (e.g., due to a database crash, network issue, or insufficient funds exception thrown midway), 
 * all changes must be rolled back. The {@code @Transactional} annotation applied at the Service layer ensures these 
 * operations happen as a single, atomic unit of work (ACID properties), preventing scenarios where money is deducted but never credited.</p>
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_bank_account_id", nullable = false)
    private BankAccount senderBankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_bank_account_id", nullable = false)
    private BankAccount receiverBankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_upi_id", nullable = false)
    private UpiId senderUpiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_upi_id", nullable = false)
    private UpiId receiverUpiId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public BankAccount getSenderBankAccount() {
        return senderBankAccount;
    }

    public void setSenderBankAccount(BankAccount senderBankAccount) {
        this.senderBankAccount = senderBankAccount;
    }

    public BankAccount getReceiverBankAccount() {
        return receiverBankAccount;
    }

    public void setReceiverBankAccount(BankAccount receiverBankAccount) {
        this.receiverBankAccount = receiverBankAccount;
    }

    public UpiId getSenderUpiId() {
        return senderUpiId;
    }

    public void setSenderUpiId(UpiId senderUpiId) {
        this.senderUpiId = senderUpiId;
    }

    public UpiId getReceiverUpiId() {
        return receiverUpiId;
    }

    public void setReceiverUpiId(UpiId receiverUpiId) {
        this.receiverUpiId = receiverUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
