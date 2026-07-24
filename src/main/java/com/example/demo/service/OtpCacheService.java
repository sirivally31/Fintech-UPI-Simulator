package com.example.demo.service;

/**
 * Service contract for generating, storing, and verifying one-time passwords (OTP) in Redis.
 */
public interface OtpCacheService {

    String generateOtp(String identifier);

    boolean verifyOtp(String identifier, String inputOtp);
}
