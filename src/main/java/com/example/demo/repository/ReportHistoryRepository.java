package com.example.demo.repository;

import com.example.demo.entity.ReportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {

    Page<ReportHistory> findByGeneratedBy(String generatedBy, Pageable pageable);
    
    Page<ReportHistory> findByGeneratedTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
