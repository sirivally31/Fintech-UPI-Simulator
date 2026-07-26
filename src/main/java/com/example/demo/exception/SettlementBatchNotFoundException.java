package com.example.demo.exception;

public class SettlementBatchNotFoundException extends RuntimeException {
    public SettlementBatchNotFoundException(String message) {
        super(message);
    }
}
