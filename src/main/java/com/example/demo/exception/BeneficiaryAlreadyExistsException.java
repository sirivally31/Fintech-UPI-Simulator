package com.example.demo.exception;

public class BeneficiaryAlreadyExistsException extends RuntimeException {
    public BeneficiaryAlreadyExistsException(String message) {
        super(message);
    }
}
