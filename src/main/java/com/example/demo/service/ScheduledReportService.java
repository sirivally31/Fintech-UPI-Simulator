package com.example.demo.service;

import com.example.demo.entity.ReportHistory;
import com.example.demo.events.ScheduledReportGeneratedEvent;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.ReportHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledReportService {

    private final ReportHistoryRepository reportHistoryRepository;
    private final EventPublisher eventPublisher;

    public ScheduledReportService(ReportHistoryRepository reportHistoryRepository, EventPublisher eventPublisher) {
        this.reportHistoryRepository = reportHistoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void generateDailyReport() {
        generateAndSave("Daily System Report", "DAILY");
    }

    @Scheduled(cron = "0 0 2 * * MON") // Weekly on Monday at 2 AM
    public void generateWeeklyReport() {
        generateAndSave("Weekly System Report", "WEEKLY");
    }

    @Scheduled(cron = "0 0 3 1 * ?") // Monthly on the 1st at 3 AM
    public void generateMonthlyReport() {
        generateAndSave("Monthly System Report", "MONTHLY");
    }

    private void generateAndSave(String reportName, String type) {
        ReportHistory history = new ReportHistory(reportName, "SYSTEM", "PDF", "type=" + type, "GENERATED");
        history = reportHistoryRepository.save(history);
        eventPublisher.publishScheduledReportGenerated(new ScheduledReportGeneratedEvent(history.getId(), type));
    }
}
