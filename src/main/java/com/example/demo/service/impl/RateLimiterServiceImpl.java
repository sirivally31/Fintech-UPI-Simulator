package com.example.demo.service.impl;

import com.example.demo.exception.RateLimitExceededException;
import com.example.demo.service.RateLimiterService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of RateLimiterService using Redis atomic counters and sliding window expirations.
 */
@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterServiceImpl.class);
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private final RedisCacheService redisCacheService;

    public RateLimiterServiceImpl(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void checkRateLimit(String identifier, String action, int maxRequests, long windowSeconds) {
        String rateLimitKey = RATE_LIMIT_PREFIX + action + ":" + identifier;

        Long currentCount = redisCacheService.increment(rateLimitKey, 1L);

        if (currentCount != null && currentCount == 1L) {
            redisCacheService.expire(rateLimitKey, windowSeconds, TimeUnit.SECONDS);
        }

        if (currentCount != null && currentCount > maxRequests) {
            log.warn("Rate Limit Exceeded | identifier: [{}] | action: [{}] | currentCount: [{}] | limit: [{}]",
                    identifier, action, currentCount, maxRequests);
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded for action [%s]. Allowed: %d requests per %d seconds.",
                            action, maxRequests, windowSeconds)
            );
        }
    }
}
