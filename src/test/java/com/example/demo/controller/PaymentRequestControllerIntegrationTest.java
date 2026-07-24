package com.example.demo.controller;

import com.example.demo.config.AbstractIntegrationTest;
import com.example.demo.dto.*;
import com.example.demo.entity.AccountType;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.PaymentRequestStatus;
import com.example.demo.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Enterprise Integration Test for PaymentRequestController against Testcontainers.
 */
@AutoConfigureMockMvc
class PaymentRequestControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UpiIdRepository upiIdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String requesterJwt;
    private String payerJwt;
    private String payerUpiIdStr;

    @BeforeEach
    void setUp() throws Exception {
        paymentRequestRepository.deleteAll();
        transactionRepository.deleteAll();
        upiIdRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
        outboxEventRepository.deleteAll();

        // 1. Requester Setup
        UserCreateDto reqUser = new UserCreateDto("Requester", "9777777777", "9777777777", "1111", new BigDecimal("100.00"));
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reqUser)))
                .andExpect(status().isCreated());

        LoginRequestDto reqLoginDto = new LoginRequestDto();
        reqLoginDto.setUpiId("9777777777");
        reqLoginDto.setPin("1111");
        MvcResult reqLogin = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqLoginDto)))
                .andExpect(status().isOk()).andReturn();
        this.requesterJwt = objectMapper.readTree(reqLogin.getResponse().getContentAsString()).get("token").asText();

        CreateBankAccountRequest reqBank = new CreateBankAccountRequest();
        reqBank.setBankName("HDFC"); reqBank.setIfscCode("HDFC0001111"); reqBank.setAccountType(AccountType.SAVINGS); reqBank.setBalance(new BigDecimal("100.00"));
        MvcResult reqBankRes = mockMvc.perform(post("/api/accounts").header("Authorization", "Bearer " + requesterJwt).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reqBank)))
                .andExpect(status().isCreated()).andReturn();
        Long reqBankId = objectMapper.readTree(reqBankRes.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/upi").header("Authorization", "Bearer " + requesterJwt).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateUpiIdRequest() {{ setBankAccountId(reqBankId); setPreferredHandle("reqpay"); }})))
                .andExpect(status().isCreated());

        // 2. Payer Setup
        UserCreateDto payerUser = new UserCreateDto("Payer", "9666666666", "9666666666", "2222", new BigDecimal("1000.00"));
        mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payerUser)))
                .andExpect(status().isCreated());

        LoginRequestDto payerLoginDto = new LoginRequestDto();
        payerLoginDto.setUpiId("9666666666");
        payerLoginDto.setPin("2222");
        MvcResult payerLogin = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payerLoginDto)))
                .andExpect(status().isOk()).andReturn();
        this.payerJwt = objectMapper.readTree(payerLogin.getResponse().getContentAsString()).get("token").asText();

        CreateBankAccountRequest payerBank = new CreateBankAccountRequest();
        payerBank.setBankName("ICICI"); payerBank.setIfscCode("ICIC0002222"); payerBank.setAccountType(AccountType.SAVINGS); payerBank.setBalance(new BigDecimal("1000.00"));
        MvcResult payerBankRes = mockMvc.perform(post("/api/accounts").header("Authorization", "Bearer " + payerJwt).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(payerBank)))
                .andExpect(status().isCreated()).andReturn();
        Long payerBankId = objectMapper.readTree(payerBankRes.getResponse().getContentAsString()).get("id").asLong();

        BankAccount payerAcc = bankAccountRepository.findById(payerBankId).orElseThrow();
        payerAcc.setUpiPin(passwordEncoder.encode("2222"));
        bankAccountRepository.save(payerAcc);

        mockMvc.perform(post("/api/upi").header("Authorization", "Bearer " + payerJwt).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateUpiIdRequest() {{ setBankAccountId(payerBankId); setPreferredHandle("payerpay"); }})))
                .andExpect(status().isCreated());

        this.payerUpiIdStr = upiIdRepository.findAll().stream().filter(u -> u.getUpiId().contains("payerpay")).findFirst().orElseThrow().getUpiId();
    }

    @Test
    @DisplayName("Should create payment collect request successfully")
    void shouldCreatePaymentRequestSuccessfully() throws Exception {
        CreatePaymentRequestRequest createReq = new CreatePaymentRequestRequest();
        createReq.setReceiverUpiId(payerUpiIdStr);
        createReq.setAmount(new BigDecimal("150.00"));
        createReq.setNote("Dinner split");

        mockMvc.perform(post("/api/payment-requests/")
                        .header("Authorization", "Bearer " + requesterJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestReference", notNullValue()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertEquals(1, paymentRequestRepository.count());
    }

    @Test
    @DisplayName("Should accept payment request and trigger money transfer")
    void shouldAcceptPaymentRequestSuccessfully() throws Exception {
        // Create request
        CreatePaymentRequestRequest createReq = new CreatePaymentRequestRequest();
        createReq.setReceiverUpiId(payerUpiIdStr);
        createReq.setAmount(new BigDecimal("200.00"));
        createReq.setNote("Movie ticket");

        MvcResult createRes = mockMvc.perform(post("/api/payment-requests/")
                .header("Authorization", "Bearer " + requesterJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated()).andReturn();

        String ref = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("requestReference").asText();

        // Payer accepts request
        AcceptPaymentRequestRequest acceptReq = new AcceptPaymentRequestRequest();
        acceptReq.setUpiPin("2222");

        mockMvc.perform(put("/api/payment-requests/" + ref + "/accept")
                        .header("Authorization", "Bearer " + payerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // Verify status in DB
        assertEquals(PaymentRequestStatus.ACCEPTED, paymentRequestRepository.findByRequestReference(ref).orElseThrow().getStatus());
        assertEquals(1, transactionRepository.count());
    }

    @Test
    @DisplayName("Should reject payment request successfully")
    void shouldRejectPaymentRequest() throws Exception {
        CreatePaymentRequestRequest createReq = new CreatePaymentRequestRequest();
        createReq.setReceiverUpiId(payerUpiIdStr);
        createReq.setAmount(new BigDecimal("50.00"));
        createReq.setNote("Coffee");

        MvcResult createRes = mockMvc.perform(post("/api/payment-requests/")
                .header("Authorization", "Bearer " + requesterJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated()).andReturn();

        String ref = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("requestReference").asText();

        RejectPaymentRequestRequest rejectReq = new RejectPaymentRequestRequest();
        rejectReq.setReason("Wrong request");

        mockMvc.perform(put("/api/payment-requests/" + ref + "/reject")
                        .header("Authorization", "Bearer " + payerJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        assertEquals(PaymentRequestStatus.REJECTED, paymentRequestRepository.findByRequestReference(ref).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Should cancel payment request by requester")
    void shouldCancelPaymentRequest() throws Exception {
        CreatePaymentRequestRequest createReq = new CreatePaymentRequestRequest();
        createReq.setReceiverUpiId(payerUpiIdStr);
        createReq.setAmount(new BigDecimal("75.00"));

        MvcResult createRes = mockMvc.perform(post("/api/payment-requests/")
                .header("Authorization", "Bearer " + requesterJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated()).andReturn();

        String ref = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("requestReference").asText();

        CancelPaymentRequestRequest cancelReq = new CancelPaymentRequestRequest();

        mockMvc.perform(put("/api/payment-requests/" + ref + "/cancel")
                        .header("Authorization", "Bearer " + requesterJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(PaymentRequestStatus.CANCELLED, paymentRequestRepository.findByRequestReference(ref).orElseThrow().getStatus());
    }
}
