package com.example.demo.exception;

/**
 * Exception thrown when a user exceeds configured rate limits for API requests.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
