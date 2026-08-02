package com.example.demo.service.impl;

import com.example.demo.dto.AuditLogResponse;
import com.example.demo.dto.AuditSearchRequest;
import com.example.demo.dto.AuditSummaryResponse;
import com.example.demo.entity.AuditAction;
import com.example.demo.entity.AuditLog;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AuditLogRepository;
import com.example.demo.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog(
                "john_doe",
                1L,
                AuditAction.TRANSFER,
                "MONEY_TRANSFER",
                "Transaction",
                "TXN1001",
                "POST",
                "/api/v1/transfers",
                "127.0.0.1",
                "JUnit",
                "{}",
                "SUCCESS",
                200,
                true,
                25L
        );
        auditLog.setId(10L);
    }

    @Test
    @DisplayName("Should successfully save audit log and dispatch outbox event")
    void testLog_Success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        AuditLogResponse response = auditService.log(auditLog);

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
        assertEquals(AuditAction.TRANSFER, response.getAction());
        assertEquals("MONEY_TRANSFER", response.getModule());
        assertTrue(response.getSuccess());

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
        verify(outboxService, times(1)).saveOutboxEvent(any(), eq("AUDIT_LOG"), eq(10L), eq("AUDIT_CREATED"), anyString(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when logging null audit log")
    void testLog_NullEntity() {
        assertThrows(IllegalArgumentException.class, () -> auditService.log(null));
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find audit log by ID")
    void testGetById_Success() {
        when(auditLogRepository.findById(10L)).thenReturn(Optional.of(auditLog));

        AuditLogResponse response = auditService.getById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("john_doe", response.getUsername());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
    void testGetById_NotFound() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> auditService.getById(99L));
    }

    @Test
    @DisplayName("Should search audit logs with Specification and return Page")
    void testSearch_Success() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        AuditSearchRequest request = new AuditSearchRequest();
        request.setUsername("john_doe");
        request.setAction(AuditAction.TRANSFER);

        Page<AuditLogResponse> responsePage = auditService.search(request);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals("john_doe", responsePage.getContent().get(0).getUsername());
    }

    @Test
    @DisplayName("Should calculate summary statistics correctly")
    void testGetSummary_Success() {
        when(auditLogRepository.count()).thenReturn(100L);
        when(auditLogRepository.countBySuccess(true)).thenReturn(95L);
        when(auditLogRepository.countBySuccess(false)).thenReturn(5L);

        List<Object[]> moduleGroupResults = new ArrayList<>();
        moduleGroupResults.add(new Object[]{"MONEY_TRANSFER", 60L});
        moduleGroupResults.add(new Object[]{"AUTH", 40L});
        when(auditLogRepository.countLogEntriesByModule()).thenReturn(moduleGroupResults);

        AuditSummaryResponse summary = auditService.getSummary();

        assertNotNull(summary);
        assertEquals(100L, summary.getTotalLogs());
        assertEquals(95L, summary.getSuccessfulLogs());
        assertEquals(5L, summary.getFailedLogs());
        assertEquals(60L, summary.getModuleCounts().get("MONEY_TRANSFER"));
        assertEquals(40L, summary.getModuleCounts().get("AUTH"));
    }

    @Test
    @DisplayName("Should return recent audit logs")
    void testGetRecent_Success() {
        when(auditLogRepository.findTop20ByOrderByTimestampDesc()).thenReturn(List.of(auditLog));

        List<AuditLogResponse> recent = auditService.getRecent();

        assertNotNull(recent);
        assertEquals(1, recent.size());
        assertEquals(10L, recent.get(0).getId());
    }

    @Test
    @DisplayName("Should generate CSV export bytes")
    void testExport_Success() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        AuditSearchRequest request = new AuditSearchRequest();
        byte[] csvBytes = auditService.export(request);

        assertNotNull(csvBytes);
        String csvContent = new String(csvBytes);
        assertTrue(csvContent.contains("ID,EventID,Timestamp,Username,Action"));
        assertTrue(csvContent.contains("john_doe"));
        assertTrue(csvContent.contains("MONEY_TRANSFER"));
    }
}
