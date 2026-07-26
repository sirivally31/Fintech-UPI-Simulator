package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Aggregated system statistics and operational metrics for the Admin Dashboard")
public class DashboardSummaryResponse {

    @Schema(description = "Total registered users", example = "150")
    private long totalUsers;

    @Schema(description = "Active user count", example = "142")
    private long activeUsers;

    @Schema(description = "Total linked bank accounts", example = "210")
    private long totalBankAccounts;

    @Schema(description = "Total registered merchants", example = "45")
    private long totalMerchants;

    @Schema(description = "Total transactions processed", example = "12500")
    private long totalTransactions;

    @Schema(description = "Successful transactions count", example = "12100")
    private long successfulTransactions;

    @Schema(description = "Failed transactions count", example = "400")
    private long failedTransactions;

    @Schema(description = "Total QR payments executed", example = "3400")
    private long totalQrPayments;

    @Schema(description = "Active AutoPay mandates count", example = "520")
    private long autoPayCount;

    @Schema(description = "Fraud alerts count", example = "18")
    private long fraudAlerts;

    @Schema(description = "Total notifications sent", example = "24000")
    private long notificationCount;

    @Schema(description = "Total transaction volume processed today", example = "1500000.00")
    private BigDecimal todayVolume;

    @Schema(description = "Total transaction count today", example = "450")
    private long todayTransactionCount;

    @Schema(description = "Total pending merchant settlements", example = "12")
    private long totalSettlementPending;

    @Schema(description = "Kafka cluster status", example = "UP")
    private String kafkaStatus;

    @Schema(description = "Redis cache cluster status", example = "UP")
    private String redisStatus;

    @Schema(description = "Database cluster status", example = "UP")
    private String databaseStatus;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long totalUsers, long activeUsers, long totalBankAccounts, 
                                    long totalMerchants, long totalTransactions, long successfulTransactions, 
                                    long failedTransactions, long totalQrPayments, long autoPayCount, 
                                    long fraudAlerts, long notificationCount, BigDecimal todayVolume, 
                                    long todayTransactionCount, long totalSettlementPending, 
                                    String kafkaStatus, String redisStatus, String databaseStatus) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalBankAccounts = totalBankAccounts;
        this.totalMerchants = totalMerchants;
        this.totalTransactions = totalTransactions;
        this.successfulTransactions = successfulTransactions;
        this.failedTransactions = failedTransactions;
        this.totalQrPayments = totalQrPayments;
        this.autoPayCount = autoPayCount;
        this.fraudAlerts = fraudAlerts;
        this.notificationCount = notificationCount;
        this.todayVolume = todayVolume;
        this.todayTransactionCount = todayTransactionCount;
        this.totalSettlementPending = totalSettlementPending;
        this.kafkaStatus = kafkaStatus;
        this.redisStatus = redisStatus;
        this.databaseStatus = databaseStatus;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getTotalBankAccounts() {
        return totalBankAccounts;
    }

    public void setTotalBankAccounts(long totalBankAccounts) {
        this.totalBankAccounts = totalBankAccounts;
    }

    public long getTotalMerchants() {
        return totalMerchants;
    }

    public void setTotalMerchants(long totalMerchants) {
        this.totalMerchants = totalMerchants;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(long successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public long getFailedTransactions() {
        return failedTransactions;
    }

    public void setFailedTransactions(long failedTransactions) {
        this.failedTransactions = failedTransactions;
    }

    public long getTotalQrPayments() {
        return totalQrPayments;
    }

    public void setTotalQrPayments(long totalQrPayments) {
        this.totalQrPayments = totalQrPayments;
    }

    public long getAutoPayCount() {
        return autoPayCount;
    }

    public void setAutoPayCount(long autoPayCount) {
        this.autoPayCount = autoPayCount;
    }

    public long getFraudAlerts() {
        return fraudAlerts;
    }

    public void setFraudAlerts(long fraudAlerts) {
        this.fraudAlerts = fraudAlerts;
    }

    public long getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(long notificationCount) {
        this.notificationCount = notificationCount;
    }

    public BigDecimal getTodayVolume() {
        return todayVolume;
    }

    public void setTodayVolume(BigDecimal todayVolume) {
        this.todayVolume = todayVolume;
    }

    public long getTodayTransactionCount() {
        return todayTransactionCount;
    }

    public void setTodayTransactionCount(long todayTransactionCount) {
        this.todayTransactionCount = todayTransactionCount;
    }

    public long getTotalSettlementPending() {
        return totalSettlementPending;
    }

    public void setTotalSettlementPending(long totalSettlementPending) {
        this.totalSettlementPending = totalSettlementPending;
    }

    public String getKafkaStatus() {
        return kafkaStatus;
    }

    public void setKafkaStatus(String kafkaStatus) {
        this.kafkaStatus = kafkaStatus;
    }

    public String getRedisStatus() {
        return redisStatus;
    }

    public void setRedisStatus(String redisStatus) {
        this.redisStatus = redisStatus;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }
}
