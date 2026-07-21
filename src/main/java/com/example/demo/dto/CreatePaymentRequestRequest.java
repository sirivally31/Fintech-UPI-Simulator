package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating a new Payment Request.
 * 
 * WHY A DTO (Data Transfer Object) IS REQUIRED HERE:
 * When a client calls our API, they should not send a direct representation of 
 * our database Entity (PaymentRequest). The Entity contains fields like 'status', 
 * 'createdAt', and 'id' that the client has no business modifying. A dedicated 
 * Request DTO strictly controls exactly what the client is allowed to send.
 */
@Data
public class CreatePaymentRequestRequest {

    @Schema(description = "The UPI ID of the person you are requesting money from", example = "alice@upi")
    @NotBlank(message = "Receiver UPI ID cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$", message = "Invalid UPI ID format")
    private String receiverUpiId;

    @Schema(description = "The amount to request", example = "500.50")
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    private BigDecimal amount;

    @Schema(description = "Optional note for the request", example = "For last night's dinner")
    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;
}
