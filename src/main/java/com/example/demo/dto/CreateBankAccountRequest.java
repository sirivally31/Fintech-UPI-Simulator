package com.example.demo.dto;

import com.example.demo.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for handling Bank Account creation requests.
 * 
 * Why DTOs are used instead of returning/accepting entities:
 * DTOs (Data Transfer Objects) are specifically designed for transferring data between the client and server.
 * They allow us to decouple our external API contract from our internal database schema. If the database schema
 * changes, we can update the internal mapping without breaking the API clients.
 * 
 * Security benefits of DTOs:
 * DTOs prevent "Over-Posting" (or Mass Assignment) attacks. If we accepted the Entity directly in the request body, 
 * a malicious user could pass extra JSON fields (like 'id', 'status', 'createdAt', or 'user_id') attempting to 
 * overwrite internal state they shouldn't control. DTOs restrict incoming data strictly to what is expected.
 * 
 * How DTOs reduce coupling between layers:
 * The Presentation layer (Controller) only needs to know about the DTOs, keeping it completely ignorant of the
 * Data Access layer. The Service layer bridges the gap by mapping DTOs to Entities. This modularity means we
 * can refactor the database structure without affecting the Controllers.
 */
public class CreateBankAccountRequest {

    @NotBlank(message = "Account number is required and cannot be blank.")
    @Size(min = 5, max = 20, message = "Account number must be between 5 and 20 characters long.")
    @Pattern(regexp = "^[0-9]+$", message = "Account number must contain only numeric digits.")
    private String accountNumber;

    @NotBlank(message = "Bank name is required and cannot be blank.")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters.")
    private String bankName;

    @NotBlank(message = "IFSC code is required and cannot be blank.")
    @Size(min = 11, max = 11, message = "IFSC code must be exactly 11 characters long.")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format. Expected format: 4 letters, 1 zero, 6 alphanumeric characters.")
    private String ifscCode;

    @NotNull(message = "Account type is required (e.g., SAVINGS, CURRENT).")
    private AccountType accountType;

    @NotNull(message = "Initial balance must be provided.")
    @PositiveOrZero(message = "Initial balance must be zero or a positive amount.")
    private BigDecimal balance;

    public CreateBankAccountRequest() {
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
}
