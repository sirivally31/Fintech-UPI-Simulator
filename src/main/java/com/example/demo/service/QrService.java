package com.example.demo.service;

import com.example.demo.dto.MerchantQrResponse;
import com.example.demo.dto.QrGenerateRequest;
import com.example.demo.dto.QrScanRequest;
import com.example.demo.dto.QrScanResponse;

/**
 * Service contract for Merchant QR generation, token retrieval, and scanning verification.
 */
public interface QrService {

    MerchantQrResponse generateQr(QrGenerateRequest request);

    MerchantQrResponse getQrByToken(String qrToken);

    QrScanResponse scanQr(QrScanRequest request);
}
