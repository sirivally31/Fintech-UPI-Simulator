package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for cancelling a pending Payment Request before it is accepted or rejected.
 */
@Data
public class CancelPaymentRequestRequest {

    @Schema(description = "Optional reason for cancelling the request", example = "Wrong amount, will send a new request")
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;
}
