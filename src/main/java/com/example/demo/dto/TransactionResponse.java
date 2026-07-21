package com.example.demo.dto;

import com.example.demo.entity.TransactionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing the outcome of a money transfer.
 * 
 * <h3>Architecture Principles</h3>
 * 
 * <p><b>Why entities should never be exposed directly:</b></p>
 * <p>If we return the {@code Transaction} entity directly to the client, we risk accidentally 
 * leaking internal data (like database IDs, timestamps, or full nested User objects). It also 
 * creates tight coupling, where a change to the database schema instantly breaks the API contract. 
 * DTOs (Data Transfer Objects) act as a secure, stable contract between the backend and the client.</p>
 */
@Data
public class TransactionResponse {
    private String transactionReference;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String remarks;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
