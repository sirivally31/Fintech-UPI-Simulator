package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for rejecting a pending Payment Request.
 */
@Data
public class RejectPaymentRequestRequest {

    @Schema(description = "Optional reason for rejecting the request", example = "I already paid you in cash")
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;
}
