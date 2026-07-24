package com.example.demo.controller;

import com.example.demo.config.AbstractIntegrationTest;
import com.example.demo.dto.CreateBankAccountRequest;
import com.example.demo.dto.CreateUpiIdRequest;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UserCreateDto;
import com.example.demo.entity.AccountType;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Enterprise Integration Test for UpiIdController against Testcontainers PostgreSQL.
 */
@AutoConfigureMockMvc
class UpiIdControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UpiIdRepository upiIdRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;
    private Long bankAccountId;

    @BeforeEach
    void setUp() throws Exception {
        upiIdRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Register user
        UserCreateDto userDto = new UserCreateDto(
                "UPI Test User",
                "9888877777",
                "9888877777",
                "1234",
                new BigDecimal("1000.00")
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        // 2. Login to get JWT
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUpiId("9888877777");
        loginDto.setPin("1234");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        this.jwtToken = objectMapper.readTree(responseStr).get("token").asText();

        // 3. Create Bank Account
        CreateBankAccountRequest createAccReq = new CreateBankAccountRequest();
        createAccReq.setBankName("Axis Bank");
        createAccReq.setIfscCode("UTIB0001234");
        createAccReq.setAccountType(AccountType.SAVINGS);
        createAccReq.setBalance(new BigDecimal("2500.00"));

        MvcResult accResult = mockMvc.perform(post("/api/accounts")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccReq)))
                .andExpect(status().isCreated())
                .andReturn();

        this.bankAccountId = objectMapper.readTree(accResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("Should create new UPI ID linked to bank account successfully")
    void shouldCreateUpiIdSuccessfully() throws Exception {
        CreateUpiIdRequest upiReq = new CreateUpiIdRequest();
        upiReq.setBankAccountId(bankAccountId);
        upiReq.setPreferredHandle("oksbi");

        mockMvc.perform(post("/api/upi")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upiReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.upiId", notNullValue()));

        assertEquals(1, upiIdRepository.count());
    }

    @Test
    @DisplayName("Should validate request body when handle format is invalid")
    void shouldReturnBadRequestWhenHandleIsInvalid() throws Exception {
        CreateUpiIdRequest upiReq = new CreateUpiIdRequest();
        upiReq.setBankAccountId(bankAccountId);
        upiReq.setPreferredHandle("invalid handle!"); // Special characters not allowed

        mockMvc.perform(post("/api/upi")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upiReq)))
                .andExpect(status().isBadRequest());
    }
}
