package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload to evaluate transaction risk")
public class FraudEvaluationRequest {

    @NotBlank(message = "Payer UPI ID is required")
    @Schema(description = "Payer UPI ID", example = "john@upi")
    private String payerUpiId;

    @NotBlank(message = "Payee UPI ID is required")
    @Schema(description = "Payee UPI ID", example = "alice@upi")
    private String payeeUpiId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Transaction Amount", example = "75000.00")
    private BigDecimal amount;

    @Schema(description = "Client Device Identifier", example = "DEV-IPHONE-15-PRO")
    private String deviceId;

    @Schema(description = "Client IP Address", example = "192.168.1.100")
    private String ipAddress;

    public FraudEvaluationRequest() {
    }

    public FraudEvaluationRequest(String payerUpiId, String payeeUpiId, BigDecimal amount, String deviceId, String ipAddress) {
        this.payerUpiId = payerUpiId;
        this.payeeUpiId = payeeUpiId;
        this.amount = amount;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
    }

    public String getPayerUpiId() {
        return payerUpiId;
    }

    public void setPayerUpiId(String payerUpiId) {
        this.payerUpiId = payerUpiId;
    }

    public String getPayeeUpiId() {
        return payeeUpiId;
    }

    public void setPayeeUpiId(String payeeUpiId) {
        this.payeeUpiId = payeeUpiId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
