package com.example.demo.exception;

import com.example.demo.dto.PaymentResult;

public class PaymentProcessingException extends RuntimeException {

    private final PaymentResult result;

    public PaymentProcessingException(String message) {
        super(message);
        this.result = PaymentResult.FAILED_GENERAL;
    }

    public PaymentProcessingException(String message, PaymentResult result) {
        super(message);
        this.result = result;
    }

    public PaymentResult getResult() {
        return result;
    }
}
