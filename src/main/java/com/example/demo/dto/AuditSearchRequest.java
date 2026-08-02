package com.example.demo.dto;

import com.example.demo.entity.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Request Payload for Searching and Filtering Audit Logs")
public class AuditSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Filter by username", example = "john_doe")
    private String username;

    @Schema(description = "Filter by module name", example = "MONEY_TRANSFER")
    private String module;

    @Schema(description = "Filter by audit action")
    private AuditAction action;

    @Schema(description = "Filter by success outcome", example = "true")
    private Boolean success;

    @Schema(description = "Start Date Filter (ISO format)", example = "2026-07-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @Schema(description = "End Date Filter (ISO format)", example = "2026-07-31T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @Schema(description = "Page index (0-based)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size limit", example = "20")
    private Integer size = 20;

    @Schema(description = "Field name to sort by", example = "timestamp")
    private String sortBy = "timestamp";

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC")
    private String sortDir = "DESC";

    public AuditSearchRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getPage() {
        return page != null && page >= 0 ? page : 0;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size != null && size > 0 ? size : 20;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp";
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir != null && sortDir.equalsIgnoreCase("ASC") ? "ASC" : "DESC";
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
