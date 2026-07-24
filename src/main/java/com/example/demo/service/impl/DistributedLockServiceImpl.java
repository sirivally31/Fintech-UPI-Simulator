package com.example.demo.service.impl;

import com.example.demo.service.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Implementation of DistributedLockService using Redis atomic SETNX (setIfAbsent)
 * operations to prevent concurrent duplicate payment execution.
 */
@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockServiceImpl.class);
    private static final String LOCK_PREFIX = "lock:transaction:";

    private final RedisTemplate<String, Object> redisTemplate;

    public DistributedLockServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean acquireLock(String lockKey, String lockValue, long expireSeconds) {
        String fullKey = LOCK_PREFIX + lockKey;
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(fullKey, lockValue, Duration.ofSeconds(expireSeconds));

            if (Boolean.TRUE.equals(acquired)) {
                log.info("Lock Acquired | lockKey: [{}] | lockValue: [{}] | ttl: [{}s]", fullKey, lockValue, expireSeconds);
                return true;
            } else {
                log.warn("Lock Acquisition Failed | lockKey: [{}] | lockValue: [{}]", fullKey, lockValue);
                return false;
            }
        } catch (Exception e) {
            log.error("Lock Acquisition ERROR | lockKey: [{}]", fullKey, e);
            return false;
        }
    }

    @Override
    public void releaseLock(String lockKey, String lockValue) {
        String fullKey = LOCK_PREFIX + lockKey;
        try {
            Object currentValue = redisTemplate.opsForValue().get(fullKey);
            if (currentValue != null && currentValue.toString().equals(lockValue)) {
                redisTemplate.delete(fullKey);
                log.info("Lock Released | lockKey: [{}] | lockValue: [{}]", fullKey, lockValue);
            }
        } catch (Exception e) {
            log.error("Lock Release ERROR | lockKey: [{}]", fullKey, e);
        }
    }
}
