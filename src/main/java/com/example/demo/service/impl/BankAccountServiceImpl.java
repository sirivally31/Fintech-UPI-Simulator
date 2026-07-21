package com.example.demo.service.impl;

import com.example.demo.dto.BankAccountResponse;
import com.example.demo.dto.CreateBankAccountRequest;
import com.example.demo.dto.UpdateBankAccountRequest;
import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.User;
import com.example.demo.exception.AccountNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BankAccountService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    /**
     * Constructor Injection:
     * This is the highly recommended way to inject dependencies in Spring.
     * Unlike field injection (@Autowired on fields), constructor injection ensures that 
     * this class cannot be instantiated without its required dependencies, preventing NullPointerExceptions.
     * It also makes the class easier to unit test because mock repositories can be passed directly 
     * when creating a new instance of this service.
     */
    public BankAccountServiceImpl(BankAccountRepository bankAccountRepository, UserRepository userRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BankAccountResponse createAccount(CreateBankAccountRequest request) {
        User currentUser = getCurrentUser();

        String accountNumber = generateUniqueAccountNumber();

        BankAccount bankAccount = new BankAccount();
        
        // DTO Mapping:
        // We manually map fields from the DTO to the Entity. This isolates the Entity (Database structure) 
        // from the API contract (DTO). If the frontend requirements change, we only change the DTO and this mapping, 
        // without altering the core database schema.
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setBankName(request.getBankName());
        bankAccount.setIfscCode(request.getIfscCode());
        bankAccount.setAccountType(request.getAccountType());
        bankAccount.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        bankAccount.setStatus(AccountStatus.ACTIVE);
        bankAccount.setUser(currentUser);

        // Why repositories should never contain business logic:
        // Repositories are strictly meant for defining data access queries. Business logic (like generating unique 
        // account numbers, verifying users, and setting default states) belongs in the Service layer. This maintains 
        // a clean separation of concerns, ensuring each layer has a single responsibility.
        BankAccount savedAccount = bankAccountRepository.save(bankAccount);

        return convertToResponseDTO(savedAccount);
    }

    @Override
    public List<BankAccountResponse> getAllAccounts() {
        User currentUser = getCurrentUser();
        List<BankAccount> accounts = bankAccountRepository.findAllByUser(currentUser);
        return accounts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BankAccountResponse getAccountById(Long accountId) {
        User currentUser = getCurrentUser();
        BankAccount account = bankAccountRepository.findByIdAndUser(accountId, currentUser)
                .orElseThrow(() -> new AccountNotFoundException("Bank account not found or you do not have permission to access it."));
        
        return convertToResponseDTO(account);
    }

    @Override
    @Transactional
    public BankAccountResponse updateAccount(Long accountId, UpdateBankAccountRequest request) {
        User currentUser = getCurrentUser();
        BankAccount account = bankAccountRepository.findByIdAndUser(accountId, currentUser)
                .orElseThrow(() -> new AccountNotFoundException("Bank account not found or you do not have permission to access it."));

        if (request.getBankName() != null) {
            account.setBankName(request.getBankName());
        }
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        if (request.getStatus() != null) {
            account.setStatus(request.getStatus());
        }
        // Notice we do NOT update accountNumber, balance, ifscCode, user, or createdAt here.

        BankAccount updatedAccount = bankAccountRepository.save(account);
        return convertToResponseDTO(updatedAccount);
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        User currentUser = getCurrentUser();
        BankAccount account = bankAccountRepository.findByIdAndUser(accountId, currentUser)
                .orElseThrow(() -> new AccountNotFoundException("Bank account not found or you do not have permission to access it."));
        
        bankAccountRepository.delete(account);
    }

    /**
     * Why helper methods improve readability:
     * Extracting complex or repetitive logic into small, private helper methods (like this one) 
     * keeps the public API methods clean and focused on high-level workflow. It makes the code self-documenting.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("No authenticated session found.");
        }
        
        // Assuming the username stored in the security context is the user's phone number
        String phoneNumber = authentication.getName(); 
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user could not be found in the system."));
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            // Generates a random 10-digit account number starting from 1000000000
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (bankAccountRepository.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }

    private BankAccountResponse convertToResponseDTO(BankAccount account) {
        BankAccountResponse response = new BankAccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBankName(account.getBankName());
        response.setIfscCode(account.getIfscCode());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
}
