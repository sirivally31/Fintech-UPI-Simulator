package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for creating a new UPI ID.
 * 
 * Why DTOs are used:
 * Data Transfer Objects (DTOs) are used to encapsulate data and send it from one subsystem of an application to another.
 * They help decouple the API contract from the internal database models (Entities), allowing independent evolution of both.
 * 
 * Security advantages of DTOs:
 * DTOs prevent over-posting (Mass Assignment) attacks where clients might send additional fields (like 'isAdmin' or 'balance')
 * that shouldn't be modified. They ensure only explicitly expected data is accepted.
 * 
 * Validation annotations:
 * We use Jakarta Bean Validation (@NotNull, @NotBlank, etc.) to enforce constraints at the API boundaries. 
 * This ensures invalid data is rejected before it reaches the business logic or database.
 */
@Data
public class CreateUpiIdRequest {

    @NotNull(message = "Bank Account ID is required")
    private Long bankAccountId;

    @NotBlank(message = "Preferred handle cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Preferred handle must contain only alphanumeric characters (e.g. upi, paytm, ybl)")
    private String preferredHandle;
}
