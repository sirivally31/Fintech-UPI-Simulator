package com.example.demo.dto;

import com.example.demo.entity.PaymentRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning Payment Request details back to the client.
 * 
 * WHY WE DON'T RETURN ENTITIES DIRECTLY:
 * 1. Security: We do not want to accidentally leak sensitive database properties 
 *    or passwords.
 * 2. Decoupling: If our database schema changes (e.g., column names change), 
 *    our API response remains stable because the DTO acts as a protective shield.
 * 3. Flattening: We can flatten nested relationships. Instead of returning a full 
 *    UpiId object, we just return the string 'senderUpiId'.
 */
@Data
public class PaymentRequestResponse {

    @Schema(description = "The internal database ID of the request", example = "105")
    private Long id;

    @Schema(description = "Unique string reference generated for this request", example = "REQ83920491")
    private String requestReference;

    @Schema(description = "The UPI ID of the user being asked to pay", example = "alice@upi")
    private String senderUpiId;

    @Schema(description = "The UPI ID of the user requesting the money", example = "bob@upi")
    private String receiverUpiId;

    @Schema(description = "The requested amount", example = "500.50")
    private BigDecimal amount;

    @Schema(description = "The attached note", example = "For last night's dinner")
    private String note;

    @Schema(description = "Current lifecycle status of the request", example = "PENDING")
    private PaymentRequestStatus status;

    @Schema(description = "Timestamp of when the request was initiated")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of when the request automatically expires")
    private LocalDateTime expiresAt;

    @Schema(description = "Timestamp of when the request was accepted, rejected, or cancelled (null if pending)")
    private LocalDateTime respondedAt;
}
