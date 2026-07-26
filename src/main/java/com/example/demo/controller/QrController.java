package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.QrPaymentService;
import com.example.demo.service.QrService;
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

@RestController
@RequestMapping("/api/qr")
@Tag(name = "Merchant QR Code APIs", description = "Endpoints for generating static/dynamic QR codes, scanning QR codes, executing QR payments, and viewing payment history")
public class QrController {

    private final QrService qrService;
    private final QrPaymentService qrPaymentService;

    public QrController(QrService qrService, QrPaymentService qrPaymentService) {
        this.qrService = qrService;
        this.qrPaymentService = qrPaymentService;
    }

    @Operation(summary = "Generate Merchant QR Code", description = "Generates a static or dynamic QR code with NPCI compliant upi:// payment URI.")
    @ApiResponse(responseCode = "201", description = "QR Code generated successfully",
            content = @Content(schema = @Schema(implementation = MerchantQrResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request payload or merchant inactive")
    @ApiResponse(responseCode = "404", description = "Merchant not found")
    @PostMapping("/generate")
    public ResponseEntity<MerchantQrResponse> generateQr(@Valid @RequestBody QrGenerateRequest request) {
        MerchantQrResponse response = qrService.generateQr(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get QR Details by Token", description = "Retrieves stored details and formatted payment URI for a specific QR token.")
    @ApiResponse(responseCode = "200", description = "QR details retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantQrResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid or non-existent QR token")
    @GetMapping("/{token}")
    public ResponseEntity<MerchantQrResponse> getQrByToken(
            @Parameter(description = "Unique QR token string", required = true) @PathVariable("token") String token) {
        MerchantQrResponse response = qrService.getQrByToken(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Scan Merchant QR Code", description = "Validates scanned QR token for merchant active status, expiration, and single-use status.")
    @ApiResponse(responseCode = "200", description = "QR code validated successfully and valid for payment",
            content = @Content(schema = @Schema(implementation = QrScanResponse.class)))
    @ApiResponse(responseCode = "400", description = "QR code invalid, expired, or already used")
    @PostMapping("/scan")
    public ResponseEntity<QrScanResponse> scanQr(@Valid @RequestBody QrScanRequest request) {
        QrScanResponse response = qrService.scanQr(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Execute QR Payment", description = "Executes a payment transfer against a scanned merchant QR token with UPI PIN verification.")
    @ApiResponse(responseCode = "200", description = "Payment executed successfully",
            content = @Content(schema = @Schema(implementation = PayQrResponse.class)))
    @ApiResponse(responseCode = "400", description = "Payment failed (invalid PIN, insufficient balance, expired or used QR)")
    @ApiResponse(responseCode = "404", description = "Payer or Merchant not found")
    @PostMapping("/pay")
    public ResponseEntity<PayQrResponse> payQr(@Valid @RequestBody PayQrRequest request) {
        PayQrResponse response = qrPaymentService.payQr(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get QR Payment History", description = "Retrieves payment history for QR transactions made by the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = QrTransactionHistoryResponse.class))))
    @GetMapping("/history")
    public ResponseEntity<List<QrTransactionHistoryResponse>> getQrTransactionHistory() {
        List<QrTransactionHistoryResponse> history = qrPaymentService.getQrTransactionHistory();
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get QR Transaction by ID", description = "Fetches details of a specific QR payment transaction by ID.")
    @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully",
            content = @Content(schema = @Schema(implementation = QrTransactionHistoryResponse.class)))
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/history/{transactionId}")
    public ResponseEntity<QrTransactionHistoryResponse> getQrTransactionById(
            @Parameter(description = "Transaction ID", required = true) @PathVariable("transactionId") Long transactionId) {
        QrTransactionHistoryResponse response = qrPaymentService.getQrTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }
}
