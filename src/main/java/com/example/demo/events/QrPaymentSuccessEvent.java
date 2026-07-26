package com.example.demo.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a QR code payment completes successfully.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrPaymentSuccessEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private LocalDateTime eventTime;
    private String eventType;
    private String correlationId;

    private String transactionReference;
    private String utrNumber;
    private String qrToken;
    private String payerUpiId;
    private String merchantUpiId;
    private String merchantBusinessName;
    private BigDecimal amount;
    private String status;
}
