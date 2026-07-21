package com.example.demo.exception;

public class UpiIdNotFoundException extends RuntimeException {
    public UpiIdNotFoundException(String message) {
        super(message);
    }
}
