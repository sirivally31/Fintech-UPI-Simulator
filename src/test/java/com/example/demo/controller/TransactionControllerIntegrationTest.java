package com.example.demo.controller;

import com.example.demo.config.AbstractIntegrationTest;
import com.example.demo.dto.CreateBankAccountRequest;
import com.example.demo.dto.CreateUpiIdRequest;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.SendMoneyRequest;
import com.example.demo.dto.UserCreateDto;
import com.example.demo.entity.AccountType;
import com.example.demo.entity.BankAccount;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.OutboxEventRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Enterprise Integration Test for TransactionController against Testcontainers (PostgreSQL + Kafka Outbox).
 */
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private String senderJwtToken;
    private Long senderBankAccountId;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        upiIdRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();
        outboxEventRepository.deleteAll();

        // 1. Setup Sender User
        UserCreateDto senderDto = new UserCreateDto(
                "Sender User",
                "9111111111",
                "9111111111",
                "1234",
                new BigDecimal("1000.00")
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(senderDto)))
                .andExpect(status().isCreated());

        LoginRequestDto senderLogin = new LoginRequestDto();
        senderLogin.setUpiId("9111111111");
        senderLogin.setPin("1234");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(senderLogin)))
                .andExpect(status().isOk())
                .andReturn();

        this.senderJwtToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Create Sender Bank Account
        CreateBankAccountRequest senderAccReq = new CreateBankAccountRequest();
        senderAccReq.setBankName("HDFC Bank");
        senderAccReq.setIfscCode("HDFC0001234");
        senderAccReq.setAccountType(AccountType.SAVINGS);
        senderAccReq.setBalance(new BigDecimal("1000.00"));

        MvcResult senderAccResult = mockMvc.perform(post("/api/accounts")
                .header("Authorization", "Bearer " + senderJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(senderAccReq)))
                .andExpect(status().isCreated())
                .andReturn();

        this.senderBankAccountId = objectMapper.readTree(senderAccResult.getResponse().getContentAsString()).get("id").asLong();

        // Set UPI PIN for Sender Bank Account
        BankAccount senderAcc = bankAccountRepository.findById(senderBankAccountId).orElseThrow();
        senderAcc.setUpiPin(passwordEncoder.encode("1234"));
        bankAccountRepository.save(senderAcc);

        // Create Sender primary UPI ID
        CreateUpiIdRequest senderUpiReq = new CreateUpiIdRequest();
        senderUpiReq.setBankAccountId(senderBankAccountId);
        senderUpiReq.setPreferredHandle("oksbi");
        mockMvc.perform(post("/api/upi")
                .header("Authorization", "Bearer " + senderJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(senderUpiReq)))
                .andExpect(status().isCreated());

        // 2. Setup Receiver User & Bank Account & UPI ID
        UserCreateDto receiverDto = new UserCreateDto(
                "Receiver User",
                "9222222222",
                "9222222222",
                "5678",
                new BigDecimal("500.00")
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiverDto)))
                .andExpect(status().isCreated());

        LoginRequestDto receiverLogin = new LoginRequestDto();
        receiverLogin.setUpiId("9222222222");
        receiverLogin.setPin("5678");
        MvcResult recLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiverLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String receiverJwt = objectMapper.readTree(recLoginResult.getResponse().getContentAsString()).get("token").asText();

        CreateBankAccountRequest receiverAccReq = new CreateBankAccountRequest();
        receiverAccReq.setBankName("ICICI Bank");
        receiverAccReq.setIfscCode("ICIC0005678");
        receiverAccReq.setAccountType(AccountType.SAVINGS);
        receiverAccReq.setBalance(new BigDecimal("500.00"));

        MvcResult recAccResult = mockMvc.perform(post("/api/accounts")
                .header("Authorization", "Bearer " + receiverJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiverAccReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long receiverBankAccountId = objectMapper.readTree(recAccResult.getResponse().getContentAsString()).get("id").asLong();

        CreateUpiIdRequest recUpiReq = new CreateUpiIdRequest();
        recUpiReq.setBankAccountId(receiverBankAccountId);
        recUpiReq.setPreferredHandle("okicici");
        mockMvc.perform(post("/api/upi")
                .header("Authorization", "Bearer " + receiverJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recUpiReq)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should execute atomic money transfer successfully and persist transaction & outbox event")
    void shouldTransferMoneySuccessfully() throws Exception {
        // Retrieve receiver UPI handle from repository
        String receiverUpiId = upiIdRepository.findAll().stream()
                .filter(u -> u.getUpiId().contains("okicici"))
                .findFirst().orElseThrow().getUpiId();

        SendMoneyRequest sendReq = new SendMoneyRequest();
        sendReq.setSenderBankAccountId(senderBankAccountId);
        sendReq.setReceiverUpiId(receiverUpiId);
        sendReq.setAmount(new BigDecimal("200.00"));
        sendReq.setUpiPin("1234");
        sendReq.setRemarks("Lunch payment");

        mockMvc.perform(post("/api/transactions/send")
                        .header("Authorization", "Bearer " + senderJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionReference", notNullValue()))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(200.00));

        // Verify Database Persistence
        assertEquals(1, transactionRepository.count());
        assertTrue(outboxEventRepository.count() >= 1);

        // Verify Balances (Sender: 1000 - 200 = 800, Receiver: 500 + 200 = 700)
        BankAccount updatedSender = bankAccountRepository.findById(senderBankAccountId).orElseThrow();
        assertEquals(0, new BigDecimal("800.00").compareTo(updatedSender.getBalance()));
    }

    @Test
    @DisplayName("Should fail money transfer when UPI PIN is invalid")
    void shouldFailTransferWhenUpiPinIsInvalid() throws Exception {
        String receiverUpiId = upiIdRepository.findAll().stream()
                .filter(u -> u.getUpiId().contains("okicici"))
                .findFirst().orElseThrow().getUpiId();

        SendMoneyRequest sendReq = new SendMoneyRequest();
        sendReq.setSenderBankAccountId(senderBankAccountId);
        sendReq.setReceiverUpiId(receiverUpiId);
        sendReq.setAmount(new BigDecimal("100.00"));
        sendReq.setUpiPin("9999"); // Invalid PIN

        mockMvc.perform(post("/api/transactions/send")
                        .header("Authorization", "Bearer " + senderJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().is4xxClientError());

        assertEquals(0, transactionRepository.count());
    }

    @Test
    @DisplayName("Should fail money transfer when balance is insufficient")
    void shouldFailTransferWhenBalanceIsInsufficient() throws Exception {
        String receiverUpiId = upiIdRepository.findAll().stream()
                .filter(u -> u.getUpiId().contains("okicici"))
                .findFirst().orElseThrow().getUpiId();

        SendMoneyRequest sendReq = new SendMoneyRequest();
        sendReq.setSenderBankAccountId(senderBankAccountId);
        sendReq.setReceiverUpiId(receiverUpiId);
        sendReq.setAmount(new BigDecimal("5000.00")); // Sender balance is 1000
        sendReq.setUpiPin("1234");

        mockMvc.perform(post("/api/transactions/send")
                        .header("Authorization", "Bearer " + senderJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendReq)))
                .andExpect(status().is5xxServerError());

        assertEquals(0, transactionRepository.count());
    }
}
