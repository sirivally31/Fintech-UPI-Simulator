package com.example.demo.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportDownloadedEvent {
    private String correlationId;
    private Long reportId;
    private String downloadedBy;
    private LocalDateTime timestamp;

    public ReportDownloadedEvent() {}

    public ReportDownloadedEvent(Long reportId, String downloadedBy) {
        this.correlationId = UUID.randomUUID().toString();
        this.reportId = reportId;
        this.downloadedBy = downloadedBy;
        this.timestamp = LocalDateTime.now();
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getDownloadedBy() { return downloadedBy; }
    public void setDownloadedBy(String downloadedBy) { this.downloadedBy = downloadedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
