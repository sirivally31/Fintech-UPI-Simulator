package com.example.demo.service;

import com.example.demo.dto.AddBeneficiaryRequest;
import com.example.demo.dto.BeneficiaryListResponse;
import com.example.demo.dto.BeneficiaryResponse;
import com.example.demo.dto.UpdateBeneficiaryRequest;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Beneficiary Management.
 */
public interface BeneficiaryService {

    BeneficiaryResponse addBeneficiary(AddBeneficiaryRequest request);

    BeneficiaryResponse updateBeneficiary(UUID id, UpdateBeneficiaryRequest request);

    void deleteBeneficiary(UUID id);

    BeneficiaryResponse markFavourite(UUID id, boolean favourite);

    BeneficiaryResponse verifyBeneficiary(UUID id);

    BeneficiaryResponse getBeneficiary(UUID id);

    BeneficiaryListResponse getAllBeneficiaries();

    List<BeneficiaryResponse> searchBeneficiaries(String query);
}
