package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating an existing beneficiary")
public class UpdateBeneficiaryRequest {

    @Size(min = 2, max = 100, message = "Beneficiary name must be between 2 and 100 characters")
    @Schema(description = "Updated full name of the beneficiary", example = "Alice Smith")
    private String beneficiaryName;

    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    @Schema(description = "Updated nickname for the beneficiary", example = "Alice Office")
    private String nickname;

    @Schema(description = "Flag indicating whether beneficiary is marked as favorite", example = "true")
    private Boolean favourite;

    public UpdateBeneficiaryRequest() {
    }

    public UpdateBeneficiaryRequest(String beneficiaryName, String nickname, Boolean favourite) {
        this.beneficiaryName = beneficiaryName;
        this.nickname = nickname;
        this.favourite = favourite;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
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
