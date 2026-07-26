package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a saved Beneficiary for quick UPI transfers.
 */
@Entity
@Table(name = "beneficiaries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_owner_beneficiary_upi", columnNames = {"owner_id", "beneficiary_upi_id"})
})
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String beneficiaryName;

    @Column(name = "beneficiary_upi_id", nullable = false, length = 100)
    private String beneficiaryUpiId;

    @Column(length = 100)
    private String nickname;

    @Column(nullable = false)
    private Boolean favourite = false;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Beneficiary() {
    }

    public Beneficiary(UUID id, User owner, String beneficiaryName, String beneficiaryUpiId, 
                       String nickname, Boolean favourite, Boolean verified, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.owner = owner;
        this.beneficiaryName = beneficiaryName;
        this.beneficiaryUpiId = beneficiaryUpiId;
        this.nickname = nickname;
        this.favourite = favourite;
        this.verified = verified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.favourite == null) {
            this.favourite = false;
        }
        if (this.verified == null) {
            this.verified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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
