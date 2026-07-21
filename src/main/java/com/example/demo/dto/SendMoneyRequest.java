package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for initiating a money transfer.
 * 
 * <h3>Architecture & Security Principles</h3>
 * 
 * <p><b>Why request and response DTOs should be separated:</b></p>
 * <p>A request to send money requires a UPI PIN and bank account ID, but a response confirming the 
 * transaction should NEVER return the UPI PIN, and might instead return a transaction reference. 
 * Using a single object for both request and response creates severe security vulnerabilities and 
 * couples the input shape to the output shape unnecessarily.</p>
 * 
 * <p><b>Why payment requests should be validated before reaching the service layer:</b></p>
 * <p>By using standard Bean Validation annotations (@NotNull, @DecimalMin, etc.), we ensure that 
 * malformed requests (like trying to send negative money or providing a 3-digit PIN) are instantly 
 * rejected by the Controller with a 400 Bad Request. This protects the Service layer from dealing 
 * with garbage data and reduces the load on the database.</p>
 * 
 * <p><b>Why sensitive information like UPI PIN must never be returned in responses:</b></p>
 * <p>The UPI PIN is only used for authentication. Once verified and processed, it must be discarded. 
 * If it is included in a response DTO, it could be logged in access logs, cached by proxies, or 
 * intercepted on the network, leading to account compromise.</p>
 */
@Data
public class SendMoneyRequest {

    @NotNull(message = "Sender Bank Account ID cannot be null")
    private Long senderBankAccountId;

    @NotBlank(message = "Receiver UPI ID cannot be blank")
    private String receiverUpiId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    private String remarks;

    @NotNull(message = "UPI PIN cannot be null")
    @Pattern(regexp = "^\\d{4}$", message = "UPI PIN must be exactly 4 numeric digits")
    private String upiPin;
}
