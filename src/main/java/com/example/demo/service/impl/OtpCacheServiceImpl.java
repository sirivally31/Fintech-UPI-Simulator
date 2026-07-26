package com.example.demo.service.impl;

import com.example.demo.service.OtpCacheService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.example.demo.metrics.BusinessMetricsService;

/**
 * Implementation of OtpCacheService managing 6-digit numeric OTP generation,
 * 5-minute Redis expiration, and single-use verification logic.
 */
@Service
public class OtpCacheServiceImpl implements OtpCacheService {

    private static final Logger log = LoggerFactory.getLogger(OtpCacheServiceImpl.class);
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final long OTP_TTL_MINUTES = 5;

    private final RedisCacheService redisCacheService;
    private final SecureRandom secureRandom;
    private final BusinessMetricsService businessMetricsService;

    public OtpCacheServiceImpl(RedisCacheService redisCacheService,
                               BusinessMetricsService businessMetricsService) {
        this.redisCacheService = redisCacheService;
        this.businessMetricsService = businessMetricsService;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String generateOtp(String identifier) {
        int code = 100000 + secureRandom.nextInt(900000);
        String otpStr = String.valueOf(code);
        String redisKey = OTP_KEY_PREFIX + identifier;

        redisCacheService.save(redisKey, otpStr, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("OTP Generated | identifier: [{}] | ttl: [{} minutes]", identifier, OTP_TTL_MINUTES);
        businessMetricsService.recordOtpGenerated();

        return otpStr;
    }

    @Override
    public boolean verifyOtp(String identifier, String inputOtp) {
        String redisKey = OTP_KEY_PREFIX + identifier;
        String storedOtp = redisCacheService.find(redisKey, String.class);

        if (storedOtp != null && storedOtp.equals(inputOtp)) {
            redisCacheService.delete(redisKey); // One-time use: delete immediately upon verification
            log.info("OTP Verified | identifier: [{}] | status: [SUCCESS]", identifier);
            businessMetricsService.recordOtpVerified();
            return true;
        }

        log.warn("OTP Verified | identifier: [{}] | status: [FAILED]", identifier);
        businessMetricsService.recordOtpFailed();
        return false;
    }
}
