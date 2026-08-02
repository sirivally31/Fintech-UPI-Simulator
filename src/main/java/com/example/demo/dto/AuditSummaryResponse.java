package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Map;

@Schema(description = "Summary metrics response for audit log activity")
public class AuditSummaryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Total number of recorded audit events", example = "1540")
    private long totalLogs;

    @Schema(description = "Total successful operations", example = "1480")
    private long successfulLogs;

    @Schema(description = "Total failed operations", example = "60")
    private long failedLogs;

    @Schema(description = "Count of audit logs by module")
    private Map<String, Long> moduleCounts;

    public AuditSummaryResponse() {
    }

    public AuditSummaryResponse(long totalLogs, long successfulLogs, long failedLogs, Map<String, Long> moduleCounts) {
        this.totalLogs = totalLogs;
        this.successfulLogs = successfulLogs;
        this.failedLogs = failedLogs;
        this.moduleCounts = moduleCounts;
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public long getSuccessfulLogs() {
        return successfulLogs;
    }

    public void setSuccessfulLogs(long successfulLogs) {
        this.successfulLogs = successfulLogs;
    }

    public long getFailedLogs() {
        return failedLogs;
    }

    public void setFailedLogs(long failedLogs) {
        this.failedLogs = failedLogs;
    }

    public Map<String, Long> getModuleCounts() {
        return moduleCounts;
    }

    public void setModuleCounts(Map<String, Long> moduleCounts) {
        this.moduleCounts = moduleCounts;
    }
}
