package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for verifying a UPI PIN before allowing sensitive transactions.
 * 
 * See SetUpiPinRequest for educational comments on DTO design, validation boundaries, 
 * and plain-text transmission security.
 */
@Data
public class VerifyUpiPinRequest {

    @NotNull(message = "Bank Account ID cannot be null")
    private Long bankAccountId;

    @NotNull(message = "PIN cannot be null")
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 numeric digits")
    private String pin;
}
