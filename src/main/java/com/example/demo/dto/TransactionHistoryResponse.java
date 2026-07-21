package com.example.demo.dto;

import com.example.demo.entity.TransactionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for displaying transaction history in the user dashboard.
 * 
 * <h3>Architecture Principles</h3>
 * 
 * <p><b>Why dashboard APIs usually require specialized DTOs:</b></p>
 * <p>A dashboard view might require data formatted differently than a standard transaction receipt. 
 * For instance, history endpoints often need paginated, summarized data. By creating a dedicated 
 * {@code TransactionHistoryResponse}, we can optimize the API response size and shape specifically 
 * for the UI components that render the transaction feed, without polluting the core {@code TransactionResponse}.</p>
 */
@Data
public class TransactionHistoryResponse {
    private String transactionReference;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private TransactionStatus status;
    private String remarks;
    private LocalDateTime createdAt;
}
