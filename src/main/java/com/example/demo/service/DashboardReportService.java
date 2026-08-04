package com.example.demo.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardReportService {

    // Note: In a real system, these would call various repositories to calculate real metrics.
    // We are mocking the data retrieval for the sake of the Simulator report structure.

    @Cacheable(value = "reports", key = "'daily'")
    public Map<String, Object> generateDailyReport() {
        return createMockSummary("Daily");
    }

    @Cacheable(value = "reports", key = "'weekly'")
    public Map<String, Object> generateWeeklyReport() {
        return createMockSummary("Weekly");
    }

    @Cacheable(value = "reports", key = "'monthly'")
    public Map<String, Object> generateMonthlyReport() {
        return createMockSummary("Monthly");
    }

    @Cacheable(value = "reports", key = "'quarterly'")
    public Map<String, Object> generateQuarterlyReport() {
        return createMockSummary("Quarterly");
    }

    @Cacheable(value = "reports", key = "'yearly'")
    public Map<String, Object> generateYearlyReport() {
        return createMockSummary("Yearly");
    }

    private Map<String, Object> createMockSummary(String period) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("period", period);
        summary.put("totalTransactions", 15000);
        summary.put("revenue", 250000.0);
        summary.put("settlements", 50);
        summary.put("fraudAlerts", 5);
        summary.put("topMerchants", "Merchant A, Merchant B");
        summary.put("topUsers", "User X, User Y");
        return summary;
    }
}
