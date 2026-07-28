package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Request payload for registering a new user")
public class UserCreateDto {

    @NotBlank(message = "Name cannot be empty")
    @Schema(description = "Full legal name of the user", example = "John Doe")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    @Schema(description = "10-digit mobile phone number", example = "9876543210")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @NotBlank(message = "UPI ID cannot be empty")
    @Schema(description = "Unique Virtual Payment Address / UPI ID", example = "john@upi")
    @JsonProperty("upiId")
    private String upiId;

    @NotBlank(message = "PIN cannot be empty")
    @Size(min = 4, max = 6, message = "PIN must be between 4 and 6 digits")
    @Schema(description = "4 or 6 digit UPI security PIN", example = "1234")
    @JsonProperty("pin")
    private String pin;

    @Schema(description = "Initial account balance upon registration", example = "50000.00")
    @JsonProperty("initialBalance")
    private BigDecimal initialBalance;

    public UserCreateDto() {
    }

    public UserCreateDto(String name, String phoneNumber, String upiId, String pin, BigDecimal initialBalance) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.upiId = upiId;
        this.pin = pin;
        this.initialBalance = initialBalance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
