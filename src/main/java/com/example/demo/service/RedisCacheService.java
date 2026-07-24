package com.example.demo.service;

import java.util.concurrent.TimeUnit;

/**
 * Low-level Cache Service interface for executing Redis data operations.
 */
public interface RedisCacheService {

    void save(String key, Object value, long timeout, TimeUnit unit);

    <T> T find(String key, Class<T> clazz);

    void delete(String key);

    boolean exists(String key);

    boolean expire(String key, long timeout, TimeUnit unit);

    Long increment(String key, long delta);
}
