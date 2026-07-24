package com.example.demo.controller;

import com.example.demo.config.AbstractIntegrationTest;
import com.example.demo.dto.CreateBankAccountRequest;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UserCreateDto;
import com.example.demo.entity.AccountType;
import com.example.demo.repository.BankAccountRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Enterprise Integration Test for BankAccountController against Testcontainers PostgreSQL.
 */
@AutoConfigureMockMvc
class BankAccountControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        // Register user where upiId matches phoneNumber for cross-service resolution
        UserCreateDto userDto = new UserCreateDto(
                "Bank Test User",
                "9876543210",
                "9876543210",
                "1234",
                new BigDecimal("1000.00")
        );

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        // Login to get JWT
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUpiId("9876543210");
        loginDto.setPin("1234");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseStr = loginResult.getResponse().getContentAsString();
        this.jwtToken = objectMapper.readTree(responseStr).get("token").asText();
    }

    @Test
    @DisplayName("Should create bank account successfully with authenticated JWT")
    void shouldCreateBankAccountSuccessfully() throws Exception {
        CreateBankAccountRequest createReq = new CreateBankAccountRequest();
        createReq.setBankName("HDFC Bank");
        createReq.setIfscCode("HDFC0001234");
        createReq.setAccountType(AccountType.SAVINGS);
        createReq.setBalance(new BigDecimal("1500.00"));

        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.bankName").value("HDFC Bank"))
                .andExpect(jsonPath("$.accountNumber", notNullValue()));

        assertEquals(1, bankAccountRepository.count());
    }

    @Test
    @DisplayName("Should fetch all bank accounts for authenticated user")
    void shouldFetchUserBankAccounts() throws Exception {
        // Create account
        CreateBankAccountRequest createReq = new CreateBankAccountRequest();
        createReq.setBankName("ICICI Bank");
        createReq.setIfscCode("ICIC0005678");
        createReq.setAccountType(AccountType.CURRENT);
        createReq.setBalance(new BigDecimal("2000.00"));

        mockMvc.perform(post("/api/accounts")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        // Fetch accounts
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bankName").value("ICICI Bank"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when accessing account without JWT")
    void shouldReturnUnauthorizedWhenFetchingAccountsWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }
}
