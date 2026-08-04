package com.example.demo.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduledReportGeneratedEvent {
    private String correlationId;
    private Long reportId;
    private String reportType; // DAILY, WEEKLY, MONTHLY
    private LocalDateTime timestamp;

    public ScheduledReportGeneratedEvent() {}

    public ScheduledReportGeneratedEvent(Long reportId, String reportType) {
        this.correlationId = UUID.randomUUID().toString();
        this.reportId = reportId;
        this.reportType = reportType;
        this.timestamp = LocalDateTime.now();
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
