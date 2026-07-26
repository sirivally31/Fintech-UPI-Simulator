package com.example.demo.exception;

public class FraudRuleNotFoundException extends RuntimeException {
    public FraudRuleNotFoundException(String message) {
        super(message);
    }
}
