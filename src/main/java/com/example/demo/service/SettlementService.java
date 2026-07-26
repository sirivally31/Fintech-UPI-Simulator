package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.SettlementBatch;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Settlement and Reconciliation Engine operations.
 */
public interface SettlementService {

    SettlementBatchResponse processDailySettlements();

    SettlementBatchResponse processMerchantSettlement(UUID merchantId);

    ReconciliationSummaryResponse reconcileLedger();

    void retryFailedSettlements();

    SettlementBatchResponse reverseSettlementBatch(UUID batchId);

    List<SettlementBatchResponse> getSettlementHistory();

    SettlementBatchResponse getSettlementById(UUID id);

    SettlementReportResponse getDailyReport();

    SettlementReportResponse getWeeklyReport();

    SettlementReportResponse getMonthlyReport();

    List<SettlementBatchResponse> getMerchantSettlementHistory(UUID merchantId);
}
