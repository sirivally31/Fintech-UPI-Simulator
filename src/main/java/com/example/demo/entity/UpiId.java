package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * UpiId Entity mapping to the 'upi_ids' table in the database.
 * This class represents a Virtual Payment Address (VPA) or UPI ID linked to a BankAccount.
 *
 * @Entity: Marks this Java class as a JPA entity. The JPA provider (like Hibernate) 
 *          will map this class to a database table.
 * @Table: Specifies the exact name of the table in the database. 
 *         We use "upi_ids" as it is standard practice to use plural, lower_snake_case for table names.
 */
@Entity
@Table(name = "upi_ids")
public class UpiId {

    /**
     * @Id: Specifies the primary key of an entity.
     * @GeneratedValue(strategy = GenerationType.IDENTITY): Tells JPA to rely on the database's 
     *      auto-increment column (e.g., SERIAL in PostgreSQL) to generate the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column: Used to specify the mapped column details in the database.
     * nullable = false: Ensures this column cannot be null at the database level.
     * unique = true: Ensures no two rows can have the same UPI ID (e.g., sirivally@upi).
     * length = 50: Limits the maximum length of the string to 50 characters, optimizing database storage.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String upiId;

    /**
     * boolean primitive type is used here. 
     * @Column(nullable = false): Ensures this must have a value (true or false).
     * This field identifies if this is the default UPI ID for the user's transactions.
     */
    @Column(nullable = false)
    private boolean isPrimary;

    /**
     * @Enumerated(EnumType.STRING): Tells JPA to store the Enum as a String (e.g., "ACTIVE") 
     *      in the database rather than an integer ordinal. This makes the database more readable 
     *      and prevents data corruption if Enum values are reordered later.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UpiStatus status;

    /**
     * ManyToOne relationship: "Many" UPI IDs can belong to "One" Bank Account.
     * @ManyToOne(fetch = FetchType.LAZY): Uses lazy loading for performance. 
     *      The BankAccount data will only be fetched from the database when it is explicitly accessed,
     *      saving memory and query time if we only need the UpiId details.
     * @JoinColumn: Specifies the foreign key column name in the "upi_ids" table that points 
     *      to the "bank_accounts" table's primary key.
     * nullable = false: Ensures every UPI ID must be linked to a BankAccount.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    /**
     * updatable = false: Ensures that once the row is created, the 'created_at' timestamp 
     * is never modified by JPA during subsequent updates.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Stores the timestamp of the last time this row was modified.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors

    public UpiId() {
    }

    public UpiId(String upiId, boolean isPrimary, UpiStatus status, BankAccount bankAccount) {
        this.upiId = upiId;
        this.isPrimary = isPrimary;
        this.status = status;
        this.bankAccount = bankAccount;
    }

    // Lifecycle Callbacks

    /**
     * @PrePersist: A JPA lifecycle callback that executes right before the entity 
     * is saved (inserted) into the database for the first time.
     * We use this to automatically set both createdAt and updatedAt.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @PreUpdate: A JPA lifecycle callback that executes right before the entity 
     * is updated in the database.
     * We use this to automatically refresh the updatedAt timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public UpiStatus getStatus() {
        return status;
    }

    public void setStatus(UpiStatus status) {
        this.status = status;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
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
