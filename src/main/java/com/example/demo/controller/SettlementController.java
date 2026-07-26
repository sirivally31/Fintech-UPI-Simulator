package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@Tag(name = "Settlement & Reconciliation APIs", description = "Endpoints for triggering settlement batch processing, ledger reconciliation, batch reversals, and report generation")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Operation(summary = "Process Daily Settlements", description = "Triggers daily clearing batch generation for unsettled transactions.")
    @ApiResponse(responseCode = "200", description = "Daily settlement batch processed successfully",
            content = @Content(schema = @Schema(implementation = SettlementBatchResponse.class)))
    @PostMapping("/process")
    public ResponseEntity<SettlementBatchResponse> processDailySettlements() {
        SettlementBatchResponse response = settlementService.processDailySettlements();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reconcile Ledger", description = "Executes automated quadrupled ledger debit and credit verification engine.")
    @ApiResponse(responseCode = "200", description = "Ledger reconciliation completed",
            content = @Content(schema = @Schema(implementation = ReconciliationSummaryResponse.class)))
    @PostMapping("/reconcile")
    public ResponseEntity<ReconciliationSummaryResponse> reconcileLedger() {
        ReconciliationSummaryResponse summary = settlementService.reconcileLedger();
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Retry Failed Settlements", description = "Retries execution of failed settlement batches.")
    @ApiResponse(responseCode = "204", description = "Failed settlements retried successfully")
    @PostMapping("/retry")
    public ResponseEntity<Void> retryFailedSettlements() {
        settlementService.retryFailedSettlements();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reverse Settlement Batch", description = "Reverses a specific settlement batch and updates entry states.")
    @ApiResponse(responseCode = "200", description = "Settlement batch reversed successfully",
            content = @Content(schema = @Schema(implementation = SettlementBatchResponse.class)))
    @ApiResponse(responseCode = "404", description = "Settlement batch not found")
    @PostMapping("/reverse/{batchId}")
    public ResponseEntity<SettlementBatchResponse> reverseSettlementBatch(
            @Parameter(description = "Settlement Batch UUID", required = true) @PathVariable UUID batchId) {
        SettlementBatchResponse response = settlementService.reverseSettlementBatch(batchId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Settlement Batches", description = "Retrieves all settlement batches.")
    @ApiResponse(responseCode = "200", description = "Batches retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SettlementBatchResponse.class))))
    @GetMapping
    public ResponseEntity<List<SettlementBatchResponse>> getAllSettlements() {
        List<SettlementBatchResponse> batches = settlementService.getSettlementHistory();
        return ResponseEntity.ok(batches);
    }

    @Operation(summary = "Get Settlement Batch by ID", description = "Fetches details of a specific settlement batch.")
    @ApiResponse(responseCode = "200", description = "Batch retrieved successfully",
            content = @Content(schema = @Schema(implementation = SettlementBatchResponse.class)))
    @ApiResponse(responseCode = "404", description = "Batch not found")
    @GetMapping("/{id}")
    public ResponseEntity<SettlementBatchResponse> getSettlementById(
            @Parameter(description = "Settlement Batch UUID", required = true) @PathVariable UUID id) {
        SettlementBatchResponse response = settlementService.getSettlementById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Settlement History", description = "Retrieves complete settlement execution history.")
    @ApiResponse(responseCode = "200", description = "History retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SettlementBatchResponse.class))))
    @GetMapping("/history")
    public ResponseEntity<List<SettlementBatchResponse>> getSettlementHistory() {
        List<SettlementBatchResponse> history = settlementService.getSettlementHistory();
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get Daily Settlement Report", description = "Generates daily settlement volume and reconciliation performance report.")
    @ApiResponse(responseCode = "200", description = "Daily report generated",
            content = @Content(schema = @Schema(implementation = SettlementReportResponse.class)))
    @GetMapping("/reports/daily")
    public ResponseEntity<SettlementReportResponse> getDailyReport() {
        SettlementReportResponse report = settlementService.getDailyReport();
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Get Weekly Settlement Report", description = "Generates weekly settlement volume performance report.")
    @ApiResponse(responseCode = "200", description = "Weekly report generated",
            content = @Content(schema = @Schema(implementation = SettlementReportResponse.class)))
    @GetMapping("/reports/weekly")
    public ResponseEntity<SettlementReportResponse> getWeeklyReport() {
        SettlementReportResponse report = settlementService.getWeeklyReport();
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Get Monthly Settlement Report", description = "Generates monthly settlement volume performance report.")
    @ApiResponse(responseCode = "200", description = "Monthly report generated",
            content = @Content(schema = @Schema(implementation = SettlementReportResponse.class)))
    @GetMapping("/reports/monthly")
    public ResponseEntity<SettlementReportResponse> getMonthlyReport() {
        SettlementReportResponse report = settlementService.getMonthlyReport();
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Get Merchant Settlement History", description = "Retrieves settlement history for a specific merchant.")
    @ApiResponse(responseCode = "200", description = "Merchant settlement history retrieved",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SettlementBatchResponse.class))))
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<SettlementBatchResponse>> getMerchantSettlementHistory(
            @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID merchantId) {
        List<SettlementBatchResponse> history = settlementService.getMerchantSettlementHistory(merchantId);
        return ResponseEntity.ok(history);
    }
}
