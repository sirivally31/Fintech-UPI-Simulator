package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Merchant;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;

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

    List<Transaction> searchTransactions(String status, BigDecimal minAmount, String upiId, String reference);

    User updateUserStatus(Long userId, String action);

    Merchant updateMerchantStatus(Long merchantId, String action);

    void resendNotification(UUID notificationId);
}
