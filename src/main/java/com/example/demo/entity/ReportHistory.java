package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_history")
public class ReportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String reportName;

    @Column(nullable = false, length = 100)
    private String generatedBy;

    @Column(nullable = false)
    private LocalDateTime generatedTime;

    @Column(nullable = false, length = 20)
    private String fileType; // PDF, EXCEL, CSV

    @Column(columnDefinition = "TEXT")
    private String filtersUsed;

    @Column(nullable = false)
    private Integer downloadCount = 0;

    @Column(nullable = false, length = 20)
    private String status; // GENERATED, FAILED, PENDING

    public ReportHistory() {
        this.generatedTime = LocalDateTime.now();
    }

    public ReportHistory(String reportName, String generatedBy, String fileType, String filtersUsed, String status) {
        this.reportName = reportName;
        this.generatedBy = generatedBy;
        this.fileType = fileType;
        this.filtersUsed = filtersUsed;
        this.status = status;
        this.generatedTime = LocalDateTime.now();
        this.downloadCount = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    public LocalDateTime getGeneratedTime() { return generatedTime; }
    public void setGeneratedTime(LocalDateTime generatedTime) { this.generatedTime = generatedTime; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getFiltersUsed() { return filtersUsed; }
    public void setFiltersUsed(String filtersUsed) { this.filtersUsed = filtersUsed; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
