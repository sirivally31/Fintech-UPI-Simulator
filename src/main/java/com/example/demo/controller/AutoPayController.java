package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.AutoPayService;
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
@RequestMapping("/api/autopay")
@Tag(name = "AutoPay & Scheduled Payment APIs", description = "Endpoints for creating, managing, pausing, resuming, and executing recurring AutoPay mandates")
public class AutoPayController {

    private final AutoPayService autoPayService;

    public AutoPayController(AutoPayService autoPayService) {
        this.autoPayService = autoPayService;
    }

    @Operation(summary = "Create AutoPay Mandate", description = "Sets up a new recurring AutoPay mandate for a beneficiary.")
    @ApiResponse(responseCode = "201", description = "AutoPay mandate created successfully",
            content = @Content(schema = @Schema(implementation = AutoPayResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error in request payload")
    @ApiResponse(responseCode = "404", description = "Beneficiary not found")
    @PostMapping
    public ResponseEntity<AutoPayResponse> createAutoPay(@Valid @RequestBody CreateAutoPayRequest request) {
        AutoPayResponse response = autoPayService.createAutoPay(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update AutoPay Mandate", description = "Updates details such as amount, frequency, or end date of an active mandate.")
    @ApiResponse(responseCode = "200", description = "Mandate updated successfully",
            content = @Content(schema = @Schema(implementation = AutoPayResponse.class)))
    @ApiResponse(responseCode = "404", description = "Mandate not found")
    @PutMapping("/{id}")
    public ResponseEntity<AutoPayResponse> updateAutoPay(
            @Parameter(description = "AutoPay Mandate UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateAutoPayRequest request) {
        AutoPayResponse response = autoPayService.updateAutoPay(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel AutoPay Mandate", description = "Cancels an existing AutoPay mandate.")
    @ApiResponse(responseCode = "204", description = "Mandate cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Mandate not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAutoPay(
            @Parameter(description = "AutoPay Mandate UUID", required = true) @PathVariable UUID id) {
        autoPayService.cancelAutoPay(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pause AutoPay Mandate", description = "Pauses execution of an active AutoPay mandate.")
    @ApiResponse(responseCode = "200", description = "Mandate paused successfully",
            content = @Content(schema = @Schema(implementation = AutoPayResponse.class)))
    @ApiResponse(responseCode = "404", description = "Mandate not found")
    @PatchMapping("/{id}/pause")
    public ResponseEntity<AutoPayResponse> pauseAutoPay(
            @Parameter(description = "AutoPay Mandate UUID", required = true) @PathVariable UUID id) {
        AutoPayResponse response = autoPayService.pauseAutoPay(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resume AutoPay Mandate", description = "Resumes execution of a paused AutoPay mandate.")
    @ApiResponse(responseCode = "200", description = "Mandate resumed successfully",
            content = @Content(schema = @Schema(implementation = AutoPayResponse.class)))
    @ApiResponse(responseCode = "404", description = "Mandate not found")
    @PatchMapping("/{id}/resume")
    public ResponseEntity<AutoPayResponse> resumeAutoPay(
            @Parameter(description = "AutoPay Mandate UUID", required = true) @PathVariable UUID id) {
        AutoPayResponse response = autoPayService.resumeAutoPay(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Mandates", description = "Retrieves all AutoPay mandates created by the authenticated user.")
    @ApiResponse(responseCode = "200", description = "List of mandates retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AutoPayResponse.class))))
    @GetMapping
    public ResponseEntity<List<AutoPayResponse>> getAllAutoPays() {
        List<AutoPayResponse> responseList = autoPayService.getAllAutoPays();
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Get Mandate by ID", description = "Fetches a specific AutoPay mandate by UUID.")
    @ApiResponse(responseCode = "200", description = "Mandate retrieved successfully",
            content = @Content(schema = @Schema(implementation = AutoPayResponse.class)))
    @ApiResponse(responseCode = "404", description = "Mandate not found")
    @GetMapping("/{id}")
    public ResponseEntity<AutoPayResponse> getAutoPayById(
            @Parameter(description = "AutoPay Mandate UUID", required = true) @PathVariable UUID id) {
        AutoPayResponse response = autoPayService.getAutoPayById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get AutoPay History", description = "Retrieves execution history of AutoPay transactions.")
    @ApiResponse(responseCode = "200", description = "History retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AutoPayHistoryResponse.class))))
    @GetMapping("/history")
    public ResponseEntity<List<AutoPayHistoryResponse>> getAutoPayHistory() {
        List<AutoPayHistoryResponse> history = autoPayService.getAutoPayHistory();
        return ResponseEntity.ok(history);
    }
}
