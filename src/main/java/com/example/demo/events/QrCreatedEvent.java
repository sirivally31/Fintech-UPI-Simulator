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
 * Event published when a new Merchant QR code is generated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private LocalDateTime eventTime;
    private String eventType;
    private String correlationId;

    private String qrToken;
    private UUID merchantId;
    private String merchantCode;
    private String upiId;
    private BigDecimal amount;
    private String type;
    private LocalDateTime expiryTime;
}
