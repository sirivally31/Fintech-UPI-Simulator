package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@Schema(description = "User profile details response DTO")
public class UserDto {

    @Schema(description = "Unique internal database user ID", example = "1")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Full legal name of the user", example = "John Doe")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Registered 10-digit mobile phone number", example = "9876543210")
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @Schema(description = "Primary Virtual Payment Address / UPI ID", example = "john@upi")
    @JsonProperty("upiId")
    private String upiId;

    @Schema(description = "Current main wallet/account balance", example = "50000.00")
    @JsonProperty("balance")
    private BigDecimal balance;

    public UserDto() {
    }

    public UserDto(Long id, String name, String phoneNumber, String upiId, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.upiId = upiId;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
