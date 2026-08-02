package com.example.demo.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.demo.dto.DashboardAnalyticsResponse;

/**
 * Domain Event representing a refreshed analytics dashboard snapshot.
 */
public class DashboardRefreshedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private LocalDateTime timestamp;
    private long totalTransactions;
    private BigDecimal totalTransactionVolume;
    private String topMerchantName;
    private String topUserName;
    private String correlationId;

    public DashboardRefreshedEvent() {
    }

    public static DashboardRefreshedEvent fromAnalyticsResponse(DashboardAnalyticsResponse response) {
        DashboardRefreshedEvent event = new DashboardRefreshedEvent();
        event.eventId = UUID.randomUUID().toString();
        event.timestamp = LocalDateTime.now();
        event.totalTransactions = response != null ? response.getTotalTransactions() : 0;
        event.totalTransactionVolume = response != null ? response.getTotalTransactionVolume() : BigDecimal.ZERO;
        event.topMerchantName = response != null && response.getTopMerchants() != null && !response.getTopMerchants().isEmpty()
                ? response.getTopMerchants().get(0).getEntityName() : "N/A";
        event.topUserName = response != null && response.getTopUsers() != null && !response.getTopUsers().isEmpty()
                ? response.getTopUsers().get(0).getEntityName() : "N/A";
        event.correlationId = event.eventId;
        return event;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getTotalTransactionVolume() {
        return totalTransactionVolume;
    }

    public void setTotalTransactionVolume(BigDecimal totalTransactionVolume) {
        this.totalTransactionVolume = totalTransactionVolume;
    }

    public String getTopMerchantName() {
        return topMerchantName;
    }

    public void setTopMerchantName(String topMerchantName) {
        this.topMerchantName = topMerchantName;
    }

    public String getTopUserName() {
        return topUserName;
    }

    public void setTopUserName(String topUserName) {
        this.topUserName = topUserName;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
