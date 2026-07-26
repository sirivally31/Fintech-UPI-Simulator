package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.FraudDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fraud")
@Tag(name = "Fraud Detection & Risk Engine APIs", description = "Endpoints for managing fraud rules, evaluating transaction risk scores, and querying risk audit history")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    public FraudController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @Operation(summary = "Evaluate Transaction Risk", description = "Evaluates a proposed transfer against configured fraud rules and returns a risk score and decision (ALLOW, REVIEW, BLOCK).")
    @ApiResponse(responseCode = "200", description = "Risk evaluation completed",
            content = @Content(schema = @Schema(implementation = FraudEvaluationResult.class)))
    @ApiResponse(responseCode = "400", description = "Validation error in request payload")
    @PostMapping("/evaluate")
    public ResponseEntity<FraudEvaluationResult> evaluateTransaction(@Valid @RequestBody FraudEvaluationRequest request) {
        FraudEvaluationResult result = fraudDetectionService.evaluateTransaction(request);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get All Fraud Rules", description = "Retrieves all configured fraud rules in the Risk Engine.")
    @ApiResponse(responseCode = "200", description = "List of fraud rules retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FraudRuleResponse.class))))
    @GetMapping("/rules")
    public ResponseEntity<List<FraudRuleResponse>> getAllRules() {
        List<FraudRuleResponse> rules = fraudDetectionService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @Operation(summary = "Create Fraud Rule", description = "Adds a new fraud rule configuration to the Risk Engine.")
    @ApiResponse(responseCode = "201", description = "Fraud rule created successfully",
            content = @Content(schema = @Schema(implementation = FraudRuleResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error in request payload")
    @PostMapping("/rules")
    public ResponseEntity<FraudRuleResponse> createRule(@Valid @RequestBody CreateFraudRuleRequest request) {
        FraudRuleResponse response = fraudDetectionService.createRule(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Fraud Rule", description = "Updates an existing fraud rule's thresholds, priority, or enabled status.")
    @ApiResponse(responseCode = "200", description = "Fraud rule updated successfully",
            content = @Content(schema = @Schema(implementation = FraudRuleResponse.class)))
    @ApiResponse(responseCode = "404", description = "Fraud rule not found")
    @PutMapping("/rules/{id}")
    public ResponseEntity<FraudRuleResponse> updateRule(
            @Parameter(description = "Fraud Rule UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateFraudRuleRequest request) {
        FraudRuleResponse response = fraudDetectionService.updateRule(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Fraud Rule", description = "Deletes a fraud rule from the Risk Engine.")
    @ApiResponse(responseCode = "204", description = "Fraud rule deleted successfully")
    @ApiResponse(responseCode = "404", description = "Fraud rule not found")
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Fraud Rule UUID", required = true) @PathVariable UUID id) {
        fraudDetectionService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get Fraud Audit History", description = "Retrieves complete audit evaluation history.")
    @ApiResponse(responseCode = "200", description = "History log retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FraudLogResponse.class))))
    @GetMapping("/history")
    public ResponseEntity<List<FraudLogResponse>> getFraudHistory() {
        List<FraudLogResponse> history = fraudDetectionService.getFraudHistory();
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get High Risk Transactions", description = "Retrieves audit history of transactions flagged for REVIEW or BLOCK.")
    @ApiResponse(responseCode = "200", description = "High-risk history retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FraudLogResponse.class))))
    @GetMapping("/high-risk")
    public ResponseEntity<List<FraudLogResponse>> getHighRiskTransactions() {
        List<FraudLogResponse> highRisk = fraudDetectionService.getHighRiskTransactions();
        return ResponseEntity.ok(highRisk);
    }
}
