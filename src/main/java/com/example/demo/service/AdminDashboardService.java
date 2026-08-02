package com.example.demo.service;

import com.example.demo.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Admin Operations Dashboard and internal monitoring tools.
 */
public interface AdminDashboardService {

    DashboardSummaryResponse getDashboardSummary();

    SystemHealthResponse getSystemHealth();

    TransactionAnalyticsResponse getTransactionAnalytics();

    MerchantAnalyticsResponse getMerchantAnalytics();

    FraudAnalyticsResponse getFraudAnalytics();

    List<TransactionResponse> searchTransactions(String status, BigDecimal minAmount, String upiId, String reference);

    UserDto updateUserStatus(Long userId, String action);

    MerchantResponse updateMerchantStatus(Long merchantId, String action);

    void resendNotification(UUID notificationId);
}
