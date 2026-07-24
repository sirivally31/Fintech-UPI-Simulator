package com.example.demo.service.impl;

import com.example.demo.service.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of RedisCacheService wrapping RedisTemplate operations with structured SLF4J logging.
 */
@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheServiceImpl.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheServiceImpl(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.info("Redis Cache SAVE | key: [{}] | ttl: [{} {}]", key, timeout, unit);
        } catch (Exception e) {
            log.error("Redis Cache SAVE ERROR | key: [{}]", key, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T find(String key, Class<T> clazz) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj != null) {
                log.info("Cache HIT | key: [{}]", key);
                if (clazz.isInstance(obj)) {
                    return (T) obj;
                }
                return objectMapper.convertValue(obj, clazz);
            } else {
                log.info("Cache MISS | key: [{}]", key);
                return null;
            }
        } catch (Exception e) {
            log.error("Redis Cache FIND ERROR | key: [{}]", key, e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.info("Cache EVICT | key: [{}] | deleted: [{}]", key, deleted);
        } catch (Exception e) {
            log.error("Redis Cache DELETE ERROR | key: [{}]", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.error("Redis Cache EXISTS ERROR | key: [{}]", key, e);
            return false;
        }
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis Cache EXPIRE ERROR | key: [{}]", key, e);
            return false;
        }
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("Redis Cache INCREMENT ERROR | key: [{}]", key, e);
            return null;
        }
    }
}
