package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.AutoPay;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for AutoPay recurring payment mandate management and execution.
 */
public interface AutoPayService {

    AutoPayResponse createAutoPay(CreateAutoPayRequest request);

    AutoPayResponse updateAutoPay(UUID id, UpdateAutoPayRequest request);

    void cancelAutoPay(UUID id);

    AutoPayResponse pauseAutoPay(UUID id);

    AutoPayResponse resumeAutoPay(UUID id);

    AutoPayResponse getAutoPayById(UUID id);

    List<AutoPayResponse> getAllAutoPays();

    List<AutoPayHistoryResponse> getAutoPayHistory();

    void executeDueAutoPay(AutoPay autoPay);
}
