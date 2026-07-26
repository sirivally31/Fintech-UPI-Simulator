package com.example.demo.exception;

public class InvalidQrException extends RuntimeException {
    public InvalidQrException(String message) {
        super(message);
    }
}
