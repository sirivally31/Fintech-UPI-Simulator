package com.example.demo.service;

import com.example.demo.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Fraud Detection and Risk Engine evaluation.
 */
public interface FraudDetectionService {

    FraudEvaluationResult evaluateTransaction(FraudEvaluationRequest request);

    FraudRuleResponse createRule(CreateFraudRuleRequest request);

    FraudRuleResponse updateRule(UUID id, UpdateFraudRuleRequest request);

    void deleteRule(UUID id);

    List<FraudRuleResponse> getAllRules();

    List<FraudLogResponse> getFraudHistory();

    List<FraudLogResponse> getHighRiskTransactions();

    void recordPinFailure(String upiId);

    void resetPinFailures(String upiId);

    void blacklistUpi(String upiId);

    void blacklistDevice(String deviceId);
}
