package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for scanning a Merchant QR Code token")
public class QrScanRequest {

    @NotBlank(message = "QR Token is required")
    @Schema(description = "Unique QR token string obtained from scanning", example = "qr_9b1deb4d-3b7d-4b69-9175-2244668800aa")
    private String qrToken;

    public QrScanRequest() {
    }

    public QrScanRequest(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}
