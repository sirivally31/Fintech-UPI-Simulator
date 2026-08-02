package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for authenticating a user with UPI ID and PIN")
public class LoginRequestDto {

    @NotBlank(message = "UPI ID cannot be empty")
    @Schema(description = "Registered UPI ID / VPA of the user", example = "john@upi")
    @JsonProperty("upiId")
    @JsonAlias({"upi_id"})
    private String upiId;

    @NotBlank(message = "PIN cannot be empty")
    @Schema(description = "UPI security PIN", example = "1234")
    @JsonProperty("pin")
    private String pin;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String upiId, String pin) {
        this.upiId = upiId;
        this.pin = pin;
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
}
