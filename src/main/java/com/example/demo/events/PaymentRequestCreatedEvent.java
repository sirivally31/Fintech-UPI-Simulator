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
 * Event published when a new payment collect request is created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private LocalDateTime eventTime;
    private String eventType;
    private String correlationId;

    private String requestReference;
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String note;
    private String status;
    private LocalDateTime expiresAt;
}
