package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for accepting a pending Payment Request.
 */
@Data
public class AcceptPaymentRequestRequest {

    @Schema(description = "The secure 4-digit UPI PIN required to authorize the transfer", example = "1234")
    @NotBlank(message = "UPI PIN is required to accept a payment request")
    @Pattern(regexp = "^\\d{4}$", message = "UPI PIN must be exactly 4 numeric digits")
    private String upiPin;
}
