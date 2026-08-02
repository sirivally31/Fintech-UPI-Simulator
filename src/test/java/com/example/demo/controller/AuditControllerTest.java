package com.example.demo.controller;

import com.example.demo.dto.AuditLogResponse;
import com.example.demo.dto.AuditSearchRequest;
import com.example.demo.dto.AuditSummaryResponse;
import com.example.demo.entity.AuditAction;
import com.example.demo.entity.AuditLog;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    private ObjectMapper objectMapper;
    private AuditLogResponse auditLogResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);

        org.springframework.http.converter.ByteArrayHttpMessageConverter byteArrayConverter = new org.springframework.http.converter.ByteArrayHttpMessageConverter();

        mockMvc = MockMvcBuilders.standaloneSetup(auditController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(messageConverter, byteArrayConverter)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        AuditLog auditLog = new AuditLog(
                "admin_user",
                1L,
                AuditAction.ADMIN_ACTION,
                "ADMIN",
                "User",
                "100",
                "GET",
                "/api/admin/users",
                "127.0.0.1",
                "Mozilla",
                "{}",
                "SUCCESS",
                200,
                true,
                15L
        );
        auditLog.setId(1L);
        auditLogResponse = AuditLogResponse.fromEntity(auditLog);
    }

    @Test
    @DisplayName("GET /api/audit should return paginated audit logs")
    void testGetAuditLogs() throws Exception {
        when(auditService.search(any(AuditSearchRequest.class)))
                .thenReturn(new PageImpl<>(List.of(auditLogResponse), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/audit")
                        .param("username", "admin_user")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("admin_user"))
                .andExpect(jsonPath("$.content[0].module").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/audit/{id} should return audit log detail")
    void testGetAuditLogById() throws Exception {
        when(auditService.getById(1L)).thenReturn(auditLogResponse);

        mockMvc.perform(get("/api/audit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin_user"));
    }

    @Test
    @DisplayName("GET /api/audit/summary should return aggregated metrics")
    void testGetAuditSummary() throws Exception {
        Map<String, Long> moduleCounts = new HashMap<>();
        moduleCounts.put("ADMIN", 5L);
        AuditSummaryResponse summary = new AuditSummaryResponse(10L, 9L, 1L, moduleCounts);

        when(auditService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/audit/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLogs").value(10))
                .andExpect(jsonPath("$.successfulLogs").value(9))
                .andExpect(jsonPath("$.failedLogs").value(1));
    }

    @Test
    @DisplayName("GET /api/audit/recent should return recent audit logs")
    void testGetRecentAuditLogs() throws Exception {
        when(auditService.getRecent()).thenReturn(List.of(auditLogResponse));

        mockMvc.perform(get("/api/audit/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("admin_user"));
    }

    @Test
    @DisplayName("POST /api/audit/search should return filtered audit logs")
    void testSearchAuditLogs() throws Exception {
        when(auditService.search(any(AuditSearchRequest.class)))
                .thenReturn(new PageImpl<>(List.of(auditLogResponse), PageRequest.of(0, 10), 1));

        AuditSearchRequest searchRequest = new AuditSearchRequest();
        searchRequest.setUsername("admin_user");
        searchRequest.setAction(AuditAction.ADMIN_ACTION);

        mockMvc.perform(post("/api/audit/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("admin_user"));
    }

    @Test
    @DisplayName("GET /api/audit/export should return CSV attachment file")
    void testExportAuditLogs() throws Exception {
        byte[] csvData = "ID,EventID,Timestamp,Username\n1,uuid-1,2026-07-31,admin_user".getBytes();
        when(auditService.export(any(AuditSearchRequest.class))).thenReturn(csvData);

        mockMvc.perform(get("/api/audit/export")
                        .param("username", "admin_user"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=audit_logs_compliance_export.csv"))
                .andExpect(content().contentType("text/csv"));
    }
}
