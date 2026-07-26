package com.example.demo.service;

import com.example.demo.dto.MerchantRegisterRequest;
import com.example.demo.dto.MerchantResponse;
import com.example.demo.dto.MerchantUpdateRequest;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Merchant onboarding, lookup, updates, and management.
 */
public interface MerchantService {

    MerchantResponse registerMerchant(MerchantRegisterRequest request);

    MerchantResponse getMerchantById(UUID id);

    MerchantResponse getMerchantByCode(String merchantCode);

    MerchantResponse updateMerchant(UUID id, MerchantUpdateRequest request);

    void deleteMerchant(UUID id);

    List<MerchantResponse> getAllMerchants();
}
