package com.example.demo.service;

/**
 * Service contract for Redis-based atomic distributed locks.
 */
public interface DistributedLockService {

    boolean acquireLock(String lockKey, String lockValue, long expireSeconds);

    void releaseLock(String lockKey, String lockValue);
}
