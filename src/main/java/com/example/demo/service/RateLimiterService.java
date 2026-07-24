package com.example.demo.service;

/**
 * Service contract for Redis-backed API rate limiting per user and action.
 */
public interface RateLimiterService {

    void checkRateLimit(String identifier, String action, int maxRequests, long windowSeconds);
}
