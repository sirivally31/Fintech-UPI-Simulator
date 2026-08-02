package com.example.demo.service;

import com.example.demo.dto.AuditLogResponse;
import com.example.demo.dto.AuditSearchRequest;
import com.example.demo.dto.AuditSummaryResponse;
import com.example.demo.entity.AuditLog;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service contract for enterprise audit logging, searching, caching, and compliance exporting.
 */
public interface AuditService {

    AuditLogResponse log(AuditLog auditLog);

    Page<AuditLogResponse> search(AuditSearchRequest request);

    AuditLogResponse getById(Long id);

    AuditSummaryResponse getSummary();

    List<AuditLogResponse> getRecent();

    byte[] export(AuditSearchRequest request);
}
