package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response details for a beneficiary profile")
public class BeneficiaryResponse {

    @Schema(description = "Beneficiary UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d4e5")
    private UUID id;

    @Schema(description = "Owner User ID / Username", example = "john@upi")
    private String ownerUpiId;

    @Schema(description = "Beneficiary Full Name", example = "Alice Smith")
    private String beneficiaryName;

    @Schema(description = "Beneficiary UPI ID", example = "alice@upi")
    private String beneficiaryUpiId;

    @Schema(description = "Optional Nickname", example = "Alice Office")
    private String nickname;

    @Schema(description = "Is favourite beneficiary", example = "true")
    private Boolean favourite;

    @Schema(description = "Is verified beneficiary", example = "true")
    private Boolean verified;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public BeneficiaryResponse() {
    }

    public BeneficiaryResponse(UUID id, String ownerUpiId, String beneficiaryName, 
                               String beneficiaryUpiId, String nickname, Boolean favourite, 
                               Boolean verified, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerUpiId = ownerUpiId;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryUpiId = beneficiaryUpiId;
        this.nickname = nickname;
        this.favourite = favourite;
        this.verified = verified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOwnerUpiId() {
        return ownerUpiId;
    }

    public void setOwnerUpiId(String ownerUpiId) {
        this.ownerUpiId = ownerUpiId;
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

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
