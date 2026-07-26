package com.example.demo.service.impl;

import com.example.demo.exception.RateLimitExceededException;
import com.example.demo.service.RateLimiterService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import com.example.demo.metrics.BusinessMetricsService;

/**
 * Implementation of RateLimiterService using Redis atomic counters and sliding window expirations.
 */
@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterServiceImpl.class);
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private final RedisCacheService redisCacheService;
    private final BusinessMetricsService businessMetricsService;

    public RateLimiterServiceImpl(RedisCacheService redisCacheService,
                                  BusinessMetricsService businessMetricsService) {
        this.redisCacheService = redisCacheService;
        this.businessMetricsService = businessMetricsService;
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
            businessMetricsService.recordRateLimitBlocked(action);
            throw new RateLimitExceededException(
                    String.format("Rate limit exceeded for action [%s]. Allowed: %d requests per %d seconds.",
                            action, maxRequests, windowSeconds)
            );
        }

        businessMetricsService.recordRateLimitAllowed(action);
    }
}
