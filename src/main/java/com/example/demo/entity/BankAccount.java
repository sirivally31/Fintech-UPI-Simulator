package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Entity marks this class as a JPA entity, meaning it will be mapped to a database table.
@Entity
// @Table specifies the name of the database table to be used for mapping.
@Table(name = "bank_accounts")
public class BankAccount {

    // @Id denotes the primary key of the entity.
    @Id
    // @GeneratedValue configures the way of increment of the specified column(field). IDENTITY means auto-incremented.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column is used to specify the mapped column for a persistent property or field. 
    // unique = true ensures no two accounts can have the same account number.
    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false, length = 11)
    private String ifscCode;

    // @Enumerated maps the enum value to the database. String means it will store the enum name (SAVINGS/CURRENT) instead of an integer.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Why the PIN must always be stored as a BCrypt hash:
     * BCrypt is a cryptographic hashing algorithm designed specifically for passwords and PINs.
     * It incorporates a random salt to protect against rainbow table attacks and uses an adaptive 
     * cost factor (key stretching) to make brute-force attacks computationally expensive and slow.
     * 
     * Why plain-text PINs should never be saved:
     * Storing plain-text PINs is a catastrophic security vulnerability. If the database is compromised, 
     * attackers would have immediate access to authorize financial transactions. Hashing ensures that 
     * even if the database leaks, the original PINs remain mathematically infeasible to recover.
     * 
     * Why the PIN is associated with the BankAccount instead of the User:
     * In the real-world UPI ecosystem, a user can have multiple bank accounts linked to their profile, 
     * and each individual bank account requires its own distinct UPI PIN. Therefore, the PIN must be 
     * modeled as an attribute of the specific BankAccount, rather than a global User attribute.
     */
    @Column(name = "upi_pin")
    private String upiPin;

    /**
     * Stores the timestamp of the most recent PIN creation or update.
     */
    @Column(name = "pin_updated_at")
    private LocalDateTime pinUpdatedAt;

    // @ManyToOne specifies a many-to-one relationship with the User entity. (Many bank accounts can belong to one user)
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn specifies the foreign key column in the bank_accounts table that references the users table.
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public BankAccount() {
    }

    public BankAccount(String accountNumber, String bankName, String ifscCode, AccountType accountType, BigDecimal balance, AccountStatus status, LocalDateTime createdAt, User user) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.ifscCode = ifscCode;
        this.accountType = accountType;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
        this.user = user;
    }

    // @PrePersist is a JPA callback that is executed before the entity manager persist operation is actually executed or cascaded.
    // It's useful to set default values like createdAt.
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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUpiPin() {
        return upiPin;
    }

    public void setUpiPin(String upiPin) {
        this.upiPin = upiPin;
    }

    public LocalDateTime getPinUpdatedAt() {
        return pinUpdatedAt;
    }

    public void setPinUpdatedAt(LocalDateTime pinUpdatedAt) {
        this.pinUpdatedAt = pinUpdatedAt;
    }
}
