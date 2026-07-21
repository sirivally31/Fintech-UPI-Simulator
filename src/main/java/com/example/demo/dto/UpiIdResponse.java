package com.example.demo.dto;

import com.example.demo.entity.UpiStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for returning UPI ID data to the client.
 * 
 * Why entities should never be returned directly:
 * 1. Security: Entities often contain sensitive information (like passwords, balances, or internal IDs) that shouldn't be exposed.
 * 2. Coupling: Returning entities directly tightly couples the API response to the database schema. Any DB change will break the API.
 * 3. Serialization Issues: Entities with relationships (OneToMany, ManyToOne) can cause infinite recursion or lazy-loading exceptions during JSON serialization.
 * 
 * By mapping the Entity to this DTO, we selectively expose only safe, relevant data (like 'bankAccountNumber' instead of the entire BankAccount entity).
 */
@Data
@Builder
public class UpiIdResponse {

    private Long id;
    private String upiId;
    private UpiStatus status;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    private String bankAccountNumber;
}
