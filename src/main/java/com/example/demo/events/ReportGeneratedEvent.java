package com.example.demo.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportGeneratedEvent {
    private String correlationId;
    private Long reportId;
    private String reportName;
    private String generatedBy;
    private LocalDateTime timestamp;

    public ReportGeneratedEvent() {}

    public ReportGeneratedEvent(Long reportId, String reportName, String generatedBy) {
        this.correlationId = UUID.randomUUID().toString();
        this.reportId = reportId;
        this.reportName = reportName;
        this.generatedBy = generatedBy;
        this.timestamp = LocalDateTime.now();
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
