package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for adding a new beneficiary")
public class AddBeneficiaryRequest {

    @NotBlank(message = "Beneficiary name is required")
    @Size(min = 2, max = 100, message = "Beneficiary name must be between 2 and 100 characters")
    @Schema(description = "Full name of the beneficiary", example = "Alice Smith")
    private String beneficiaryName;

    @NotBlank(message = "Beneficiary UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+$", message = "Invalid UPI ID format")
    @Schema(description = "UPI ID of the beneficiary", example = "alice@upi")
    private String beneficiaryUpiId;

    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    @Schema(description = "Optional nickname for the beneficiary", example = "Alice Work")
    private String nickname;

    @Schema(description = "Flag indicating whether beneficiary is marked as favorite", example = "false")
    private Boolean favourite;

    public AddBeneficiaryRequest() {
    }

    public AddBeneficiaryRequest(String beneficiaryName, String beneficiaryUpiId, String nickname, Boolean favourite) {
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryUpiId = beneficiaryUpiId;
        this.nickname = nickname;
        this.favourite = favourite;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryUpiId() {
        return beneficiaryUpiId;
    }

    public void setBeneficiaryUpiId(String beneficiaryUpiId) {
        this.beneficiaryUpiId = beneficiaryUpiId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }
}
