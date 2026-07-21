package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for changing an existing UPI PIN.
 * 
 * See SetUpiPinRequest for educational comments on DTO design, validation boundaries, 
 * and plain-text transmission security.
 */
@Data
public class ChangeUpiPinRequest {

    @NotNull(message = "Bank Account ID cannot be null")
    private Long bankAccountId;

    @NotNull(message = "Old PIN cannot be null")
    @Pattern(regexp = "^\\d{4}$", message = "Old PIN must be exactly 4 numeric digits")
    private String oldPin;

    @NotNull(message = "New PIN cannot be null")
    @Pattern(regexp = "^\\d{4}$", message = "New PIN must be exactly 4 numeric digits")
    private String newPin;

    @NotNull(message = "Confirm PIN cannot be null")
    @Pattern(regexp = "^\\d{4}$", message = "Confirm PIN must be exactly 4 numeric digits")
    private String confirmPin;
}
