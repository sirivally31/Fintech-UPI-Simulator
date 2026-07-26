package com.example.demo.exception;

public class AutoPayNotFoundException extends RuntimeException {
    public AutoPayNotFoundException(String message) {
        super(message);
    }
}
