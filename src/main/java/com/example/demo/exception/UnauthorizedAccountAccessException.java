package com.example.demo.exception;

/**
 * Exception thrown when a user attempts to view, update, or delete a bank account 
 * that they do not own or do not have sufficient permissions to access. 
 * This is primarily used to prevent Insecure Direct Object Reference (IDOR) vulnerabilities.
 */
public class UnauthorizedAccountAccessException extends RuntimeException {

    public UnauthorizedAccountAccessException(String message) {
        super(message);
    }
}
