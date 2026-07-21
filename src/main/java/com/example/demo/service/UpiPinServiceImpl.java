package com.example.demo.service;

import com.example.demo.dto.ChangeUpiPinRequest;
import com.example.demo.dto.SetUpiPinRequest;
import com.example.demo.dto.VerifyUpiPinRequest;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.User;
import com.example.demo.exception.BankAccountNotFoundException;
import com.example.demo.exception.UnauthorizedAccountAccessException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UpiPinServiceImpl implements UpiPinService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor Injection
     *
     * Why we use constructor injection instead of @Autowired on fields:
     * 1. Immutability: Dependencies can be declared as 'final', ensuring they are not modified after initialization.
     * 2. Testability: When writing unit tests, we can easily instantiate this service by passing mock dependencies 
     *    through the constructor without needing Spring's reflection mechanisms.
     * 3. Safety: It guarantees that the service cannot be instantiated without its required dependencies, preventing NullPointerExceptions at runtime.
     */
    public UpiPinServiceImpl(BankAccountRepository bankAccountRepository, 
                             UserRepository userRepository, 
                             PasswordEncoder passwordEncoder) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Helper method to retrieve the currently authenticated user.
     * 
     * SecurityContextHolder:
     * This holds the security context of the current thread, including details of the currently authenticated principal.
     * By accessing the SecurityContext, we can reliably identify the user without requiring them to send their 
     * user ID in every request, preventing spoofing.
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUpiId(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in database"));
    }

    /**
     * Helper method to fetch a bank account and verify ownership.
     * 
     * Ownership validation:
     * It is critical to verify that the logged-in user actually owns the resource they are trying to access or modify.
     * Without this, a malicious user could pass someone else's bankAccountId in the request and manipulate their data 
     * (a vulnerability known as Insecure Direct Object Reference or IDOR).
     */
    private BankAccount getOwnedBankAccount(Long bankAccountId) {
        User currentUser = getCurrentUser();
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found"));

        if (!bankAccount.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccountAccessException("You do not have permission to access this bank account");
        }
        return bankAccount;
    }

    /**
     * Why services contain business rules:
     * Services orchestrate the retrieval of data, application of domain logic, and persistence of state.
     * Keeping these rules out of Controllers ensures Controllers are solely responsible for HTTP routing.
     * Keeping them out of Repositories ensures Repositories are solely responsible for database operations.
     */
    @Transactional
    @Override
    public void setUpiPin(SetUpiPinRequest request) {
        BankAccount bankAccount = getOwnedBankAccount(request.getBankAccountId());

        if (bankAccount.getUpiPin() != null) {
            throw new IllegalStateException("UPI PIN is already set. Please use Change PIN instead.");
        }

        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new IllegalArgumentException("New PIN and Confirm PIN do not match");
        }

        /**
         * BCrypt hashing & PasswordEncoder:
         * We never store plain text PINs to protect against database leaks. 
         * PasswordEncoder (configured with BCrypt) applies a one-way hashing algorithm.
         * It automatically generates a random salt for each hash, meaning identical PINs will result in different hashes.
         */
        bankAccount.setUpiPin(passwordEncoder.encode(request.getNewPin()));
        bankAccount.setPinUpdatedAt(LocalDateTime.now());
        
        bankAccountRepository.save(bankAccount);
    }

    @Transactional
    @Override
    public void changeUpiPin(ChangeUpiPinRequest request) {
        BankAccount bankAccount = getOwnedBankAccount(request.getBankAccountId());

        if (bankAccount.getUpiPin() == null) {
            throw new IllegalStateException("UPI PIN is not set. Please use Set PIN instead.");
        }

        /**
         * Why matches() is used instead of comparing strings:
         * Because BCrypt generates a random salt for every hash, the hash stored in the DB will NOT match 
         * a newly generated hash for the same plain-text PIN using simple string comparison.
         * The matches() method extracts the salt from the stored hash, hashes the incoming plain-text PIN 
         * with that specific salt, and securely compares the results to prevent timing attacks.
         */
        if (!passwordEncoder.matches(request.getOldPin(), bankAccount.getUpiPin())) {
            throw new SecurityException("Old PIN is incorrect");
        }

        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new IllegalArgumentException("New PIN and Confirm PIN do not match");
        }

        if (request.getOldPin().equals(request.getNewPin())) {
            throw new IllegalArgumentException("New PIN cannot be the same as the Old PIN");
        }

        bankAccount.setUpiPin(passwordEncoder.encode(request.getNewPin()));
        bankAccount.setPinUpdatedAt(LocalDateTime.now());
        
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public boolean verifyUpiPin(VerifyUpiPinRequest request) {
        BankAccount bankAccount = getOwnedBankAccount(request.getBankAccountId());

        if (bankAccount.getUpiPin() == null) {
            throw new IllegalStateException("UPI PIN is not set for this account.");
        }

        return passwordEncoder.matches(request.getPin(), bankAccount.getUpiPin());
    }
}
