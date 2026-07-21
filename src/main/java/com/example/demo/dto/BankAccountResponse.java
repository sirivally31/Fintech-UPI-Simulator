package com.example.demo.dto;

import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning Bank Account data to the client.
 * 
 * Security and Design Considerations:
 * 1. The User entity is deliberately excluded from this response. If we returned the entity, 
 *    the JSON serializer would traverse the User object, potentially exposing sensitive fields 
 *    like the user's UPI PIN, total balance, or PII.
 * 2. Excluding the User entity also avoids circular reference issues (StackOverflowError) 
 *    during JSON serialization, since User has a list of BankAccounts and BankAccount has a User.
 * 3. It ensures the frontend only receives the exact payload it needs to render the UI, 
 *    reducing bandwidth overhead.
 */
public class BankAccountResponse {

    private Long id;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public BankAccountResponse() {
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
}
