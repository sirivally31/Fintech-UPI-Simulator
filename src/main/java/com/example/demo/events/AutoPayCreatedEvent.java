package com.example.demo.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoPayCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private LocalDateTime eventTime;
    private String eventType;
    private String correlationId;

    private UUID autoPayId;
    private String mandateReference;
    private String ownerUpiId;
    private String beneficiaryUpiId;
    private BigDecimal amount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
}
