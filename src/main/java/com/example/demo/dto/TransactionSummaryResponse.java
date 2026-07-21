package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for powering dashboard analytics.
 * 
 * <h3>Architecture Principles</h3>
 * 
 * <p><b>Why dashboard APIs usually require specialized DTOs:</b></p>
 * <p>Analytics and summary endpoints aggregate data rather than returning raw records. 
 * Returning the entire list of transactions and forcing the frontend to calculate totals 
 * wastes bandwidth and exposes too much data. A specialized DTO allows the database to 
 * efficiently calculate aggregates (SUM, COUNT) and send only the final numbers to the client.</p>
 */
@Data
public class TransactionSummaryResponse {
    private BigDecimal totalSent;
    private BigDecimal totalReceived;
    private Long totalTransactions;
}
