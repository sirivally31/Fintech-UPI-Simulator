package com.example.demo.dto;

import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.AccountType;
import jakarta.validation.constraints.Size;

/**
 * DTO for handling Bank Account update requests.
 * 
 * In this DTO, we purposefully exclude fields like 'accountNumber', 'ifscCode', 
 * and 'balance'. These are immutable core attributes that a user should not be able 
 * to modify through a standard update endpoint.
 */
public class UpdateBankAccountRequest {

    @Size(max = 100, message = "Bank name cannot exceed 100 characters.")
    private String bankName;

    private AccountType accountType;

    private AccountStatus status;

    public UpdateBankAccountRequest() {
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
