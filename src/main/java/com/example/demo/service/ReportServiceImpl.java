package com.example.demo.service;

import com.example.demo.entity.ReportHistory;
import com.example.demo.events.ReportGeneratedEvent;
import com.example.demo.events.ReportDownloadedEvent;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.ReportHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReportServiceImpl {

    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    private final CsvExportService csvExportService;
    private final ReportHistoryRepository reportHistoryRepository;
    private final EventPublisher eventPublisher;

    public ReportServiceImpl(PdfExportService pdfExportService,
                             ExcelExportService excelExportService,
                             CsvExportService csvExportService,
                             ReportHistoryRepository reportHistoryRepository,
                             EventPublisher eventPublisher) {
        this.pdfExportService = pdfExportService;
        this.excelExportService = excelExportService;
        this.csvExportService = csvExportService;
        this.reportHistoryRepository = reportHistoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public byte[] generateReport(String reportName, String format, String generatedBy, Map<String, String> filters) {
        // Mocking data retrieval for the requested report
        List<String> headers = Arrays.asList("ID", "Date", "Amount", "Status");
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> row1 = new HashMap<>();
        row1.put("ID", "TXN123"); row1.put("Date", "2023-10-01"); row1.put("Amount", "150.00"); row1.put("Status", "SUCCESS");
        data.add(row1);

        Map<String, String> summary = new HashMap<>();
        summary.put("Total Records", "1");
        summary.put("Filters Applied", filters != null ? filters.toString() : "None");

        byte[] fileData;
        String formatUpper = format.toUpperCase();
        
        try {
            if ("PDF".equals(formatUpper)) {
                fileData = pdfExportService.generatePdfReport(reportName, headers, data, summary);
            } else if ("EXCEL".equals(formatUpper)) {
                fileData = excelExportService.generateExcelReport(reportName, headers, data, summary);
            } else if ("CSV".equals(formatUpper)) {
                fileData = csvExportService.generateCsvReport(headers, data);
            } else {
                throw new IllegalArgumentException("Unsupported format: " + format);
            }
            
            ReportHistory history = new ReportHistory(reportName, generatedBy, formatUpper, summary.get("Filters Applied"), "GENERATED");
            history = reportHistoryRepository.save(history);
            
            eventPublisher.publishReportGenerated(new ReportGeneratedEvent(history.getId(), reportName, generatedBy));
            
            return fileData;
        } catch (Exception e) {
            ReportHistory history = new ReportHistory(reportName, generatedBy, formatUpper, summary.get("Filters Applied"), "FAILED");
            reportHistoryRepository.save(history);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    @Transactional
    public byte[] downloadReport(Long reportId, String downloadedBy) {
        ReportHistory history = reportHistoryRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
            
        history.setDownloadCount(history.getDownloadCount() + 1);
        reportHistoryRepository.save(history);
        
        eventPublisher.publishReportDownloaded(new ReportDownloadedEvent(history.getId(), downloadedBy));
        
        // Return dummy bytes since we don't store actual files in DB for this simulation.
        // In reality, this would fetch from S3 or filesystem.
        return "Dummy file content based on report history".getBytes();
    }
}
