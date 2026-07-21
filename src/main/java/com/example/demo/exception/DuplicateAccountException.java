package com.example.demo.exception;

/**
 * Exception thrown when an attempt is made to create or register a bank account 
 * that already exists in the system. For example, this should be triggered 
 * if a user tries to link an account number that is already registered to another user 
 * or their own profile.
 */
public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String message) {
        super(message);
    }
}
