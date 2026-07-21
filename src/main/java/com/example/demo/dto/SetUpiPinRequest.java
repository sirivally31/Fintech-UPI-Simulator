package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for setting a new UPI PIN for a bank account.
 * 
 * Why DTOs should never contain business logic:
 * Data Transfer Objects (DTOs) should remain plain data containers. Placing business logic here 
 * violates the Single Responsibility Principle. The Service layer is responsible for business 
 * rules (e.g., verifying if 'newPin' matches 'confirmPin').
 * 
 * Why validation belongs at the API boundary:
 * Using Jakarta Bean Validation (@NotNull, @Pattern) allows us to reject malformed data right at 
 * the controller layer before it reaches the core application logic. This saves processing time 
 * and prevents unnecessary database queries.
 * 
 * Why PINs are accepted temporarily in plain text but never stored directly:
 * Clients must transmit the PIN in plain text over a secure channel (HTTPS/TLS) so the backend can 
 * hash it. However, once hashed by the Service layer, the plain-text PIN must be discarded immediately 
 * and never persisted to the database or written to application logs.
 * 
 * Why confirmPin improves user experience:
 * While the backend can theoretically function with just 'newPin', requiring a 'confirmPin' helps prevent 
 * users from accidentally locking themselves out due to a typo when initially setting their PIN.
 */
@Data
public class SetUpiPinRequest {

    @NotNull(message = "Bank Account ID cannot be null")
    private Long bankAccountId;

    @NotNull(message = "New PIN cannot be null")
    @Pattern(regexp = "^\\d{4}$", message = "New PIN must be exactly 4 numeric digits")
    private String newPin;

    @NotNull(message = "Confirm PIN cannot be null")
    @Pattern(regexp = "^\\d{4}$", message = "Confirm PIN must be exactly 4 numeric digits")
    private String confirmPin;
}
