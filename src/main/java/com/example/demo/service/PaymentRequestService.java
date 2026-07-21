package com.example.demo.service;

import com.example.demo.dto.AcceptPaymentRequestRequest;
import com.example.demo.dto.CancelPaymentRequestRequest;
import com.example.demo.dto.CreatePaymentRequestRequest;
import com.example.demo.dto.PaymentRequestResponse;
import com.example.demo.dto.RejectPaymentRequestRequest;

import java.util.List;

/**
 * Service interface for handling Payment Requests (Collect Requests).
 */
public interface PaymentRequestService {

    PaymentRequestResponse createRequest(CreatePaymentRequestRequest request);

    List<PaymentRequestResponse> getMySentRequests();

    List<PaymentRequestResponse> getMyReceivedRequests();

    PaymentRequestResponse acceptRequest(Long id, AcceptPaymentRequestRequest request);

    PaymentRequestResponse rejectRequest(Long id, RejectPaymentRequestRequest request);

    PaymentRequestResponse cancelRequest(Long id, CancelPaymentRequestRequest request);

    PaymentRequestResponse getRequestByReference(String requestReference);
}
