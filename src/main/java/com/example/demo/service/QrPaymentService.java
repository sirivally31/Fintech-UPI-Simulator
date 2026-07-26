package com.example.demo.service;

import com.example.demo.dto.PayQrRequest;
import com.example.demo.dto.PayQrResponse;
import com.example.demo.dto.QrTransactionHistoryResponse;

import java.util.List;

/**
 * Service interface for executing QR payments and retrieving QR transaction history.
 */
public interface QrPaymentService {

    PayQrResponse payQr(PayQrRequest request);

    List<QrTransactionHistoryResponse> getQrTransactionHistory();

    QrTransactionHistoryResponse getQrTransactionById(Long transactionId);
}
