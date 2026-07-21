package com.example.demo.dto;

import com.example.demo.entity.UpiStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating an existing UPI ID.
 * 
 * Why DTOs are used:
 * To define exactly what fields a client is allowed to update. 
 * 
 * Security advantages of DTOs:
 * By using this specific DTO, we prevent the user from maliciously trying to update 
 * non-modifiable fields like the 'upiId' string itself or the linked 'bankAccountId'.
 * 
 * Validation annotations:
 * We use Jakarta Bean Validation to ensure the integrity of the data being updated.
 */
@Data
public class UpdateUpiIdRequest {

    @NotNull(message = "Primary status must be specified")
    private Boolean isPrimary;

    @NotNull(message = "Status must be specified")
    private UpiStatus status;
}
