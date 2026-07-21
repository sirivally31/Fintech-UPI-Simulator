package com.example.demo.dto;

import java.math.BigDecimal;

public class UserDto {

    private Long id;
    private String name;
    private String phoneNumber;
    private String upiId;
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
