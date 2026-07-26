package com.example.demo.service.impl;

import com.example.demo.dto.MerchantRegisterRequest;
import com.example.demo.dto.MerchantResponse;
import com.example.demo.dto.MerchantUpdateRequest;
import com.example.demo.entity.Merchant;
import com.example.demo.exception.MerchantAlreadyExistsException;
import com.example.demo.exception.MerchantNotFoundException;
import com.example.demo.repository.MerchantRepository;
import com.example.demo.service.MerchantService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Production-grade Implementation of MerchantService handling business validation,
 * database persistence, and Redis caching.
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private static final Logger log = LoggerFactory.getLogger(MerchantServiceImpl.class);
    private static final String CACHE_KEY_PREFIX = "merchant:id:";
    private static final long CACHE_TTL_MINUTES = 30;

    private final MerchantRepository merchantRepository;
    private final RedisCacheService redisCacheService;

    public MerchantServiceImpl(MerchantRepository merchantRepository,
                               RedisCacheService redisCacheService) {
        this.merchantRepository = merchantRepository;
        this.redisCacheService = redisCacheService;
    }

    @Override
    @Transactional
    public MerchantResponse registerMerchant(MerchantRegisterRequest request) {
        log.info("Registering merchant with code [{}] and UPI ID [{}]", request.getMerchantCode(), request.getUpiId());

        if (merchantRepository.existsByMerchantCode(request.getMerchantCode())) {
            throw new MerchantAlreadyExistsException("Merchant with code '" + request.getMerchantCode() + "' already exists.");
        }

        if (merchantRepository.existsByUpiId(request.getUpiId())) {
            throw new MerchantAlreadyExistsException("Merchant with UPI ID '" + request.getUpiId() + "' already exists.");
        }

        Merchant merchant = new Merchant();
        merchant.setMerchantName(request.getMerchantName());
        merchant.setBusinessName(request.getBusinessName());
        merchant.setMerchantCode(request.getMerchantCode());
        merchant.setUpiId(request.getUpiId());
        merchant.setCategory(request.getCategory());
        merchant.setActive(true);

        Merchant savedMerchant = merchantRepository.save(merchant);
        MerchantResponse response = mapToResponse(savedMerchant);

        cacheMerchant(response);
        log.info("Successfully registered merchant ID [{}] with code [{}]", savedMerchant.getId(), savedMerchant.getMerchantCode());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchantById(UUID id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        MerchantResponse cached = redisCacheService.find(cacheKey, MerchantResponse.class);
        if (cached != null) {
            log.info("Retrieved merchant ID [{}] from Redis cache", id);
            return cached;
        }

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with ID: " + id));

        MerchantResponse response = mapToResponse(merchant);
        cacheMerchant(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchantByCode(String merchantCode) {
        Merchant merchant = merchantRepository.findByMerchantCode(merchantCode)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with code: " + merchantCode));

        return mapToResponse(merchant);
    }

    @Override
    @Transactional
    public MerchantResponse updateMerchant(UUID id, MerchantUpdateRequest request) {
        log.info("Updating merchant ID [{}]", id);

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with ID: " + id));

        if (request.getMerchantName() != null && !request.getMerchantName().isBlank()) {
            merchant.setMerchantName(request.getMerchantName());
        }
        if (request.getBusinessName() != null && !request.getBusinessName().isBlank()) {
            merchant.setBusinessName(request.getBusinessName());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            merchant.setCategory(request.getCategory());
        }
        if (request.getActive() != null) {
            merchant.setActive(request.getActive());
        }

        Merchant updated = merchantRepository.save(merchant);
        MerchantResponse response = mapToResponse(updated);

        // Evict and update cache
        redisCacheService.delete(CACHE_KEY_PREFIX + id);
        cacheMerchant(response);

        log.info("Successfully updated merchant ID [{}]", id);
        return response;
    }

    @Override
    @Transactional
    public void deleteMerchant(UUID id) {
        log.info("Deactivating merchant ID [{}]", id);

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found with ID: " + id));

        merchant.setActive(false);
        merchantRepository.save(merchant);

        redisCacheService.delete(CACHE_KEY_PREFIX + id);
        log.info("Successfully deactivated merchant ID [{}]", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MerchantResponse> getAllMerchants() {
        return merchantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void cacheMerchant(MerchantResponse response) {
        if (response != null && response.getId() != null) {
            redisCacheService.save(CACHE_KEY_PREFIX + response.getId(), response, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }

    private MerchantResponse mapToResponse(Merchant merchant) {
        return new MerchantResponse(
                merchant.getId(),
                merchant.getMerchantName(),
                merchant.getBusinessName(),
                merchant.getMerchantCode(),
                merchant.getUpiId(),
                merchant.getCategory(),
                merchant.getActive(),
                merchant.getCreatedAt()
        );
    }
}
