package com.example.demo.controller;

import com.example.demo.dto.AuditLogResponse;
import com.example.demo.dto.AuditSearchRequest;
import com.example.demo.dto.AuditSummaryResponse;
import com.example.demo.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Enterprise Audit Logging REST Controller for compliance, security investigation, and activity tracking.
 * Access restricted exclusively to ADMIN users.
 */
@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logging & Compliance APIs", description = "Enterprise compliance, audit search, activity tracking, and summary analytics endpoints")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Get Paginated Audit Logs", description = "Retrieve paginated list of audit logs with optional query filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Filter by username") @RequestParam(required = false) String username,
            @Parameter(description = "Filter by module name") @RequestParam(required = false) String module,
            @Parameter(description = "Filter by success state") @RequestParam(required = false) Boolean success,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size limit") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDir) {

        AuditSearchRequest request = new AuditSearchRequest();
        request.setUsername(username);
        request.setModule(module);
        request.setSuccess(success);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        Page<AuditLogResponse> response = auditService.search(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Audit Log by ID", description = "Retrieve specific audit log entry by primary key ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit log entry found",
                    content = @Content(schema = @Schema(implementation = AuditLogResponse.class))),
            @ApiResponse(responseCode = "404", description = "Audit log entry not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<AuditLogResponse> getAuditLogById(
            @Parameter(description = "Audit Log Primary Key ID", example = "1") @PathVariable Long id) {
        AuditLogResponse auditLog = auditService.getById(id);
        return ResponseEntity.ok(auditLog);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get Audit Summary Metrics", description = "Retrieve aggregated audit statistics including total, successful, failed counts, and module breakdowns.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Summary metrics retrieved",
                    content = @Content(schema = @Schema(implementation = AuditSummaryResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<AuditSummaryResponse> getAuditSummary() {
        AuditSummaryResponse summary = auditService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get Recent Audit Activities", description = "Retrieve the 20 most recent system audit log events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recent audit logs retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<List<AuditLogResponse>> getRecentAuditLogs() {
        List<AuditLogResponse> recentAudits = auditService.getRecent();
        return ResponseEntity.ok(recentAudits);
    }

    @PostMapping("/search")
    @Operation(summary = "Search Audit Logs", description = "Perform advanced filtered search on audit logs using structured request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search query completed",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<Page<AuditLogResponse>> searchAuditLogs(@RequestBody AuditSearchRequest request) {
        Page<AuditLogResponse> results = auditService.search(request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/export")
    @Operation(summary = "Export Audit Logs CSV", description = "Download compliance CSV export for audit records matching given filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV report file generated"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<byte[]> exportAuditLogs(
            @Parameter(description = "Filter by username") @RequestParam(required = false) String username,
            @Parameter(description = "Filter by module name") @RequestParam(required = false) String module) {

        AuditSearchRequest request = new AuditSearchRequest();
        request.setUsername(username);
        request.setModule(module);
        request.setSize(1000); // Export limit for compliance report

        byte[] csvData = auditService.export(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit_logs_compliance_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvData.length)
                .body(csvData);
    }
}
