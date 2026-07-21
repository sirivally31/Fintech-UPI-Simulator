package com.example.demo.service.impl;

import com.example.demo.dto.CreateUpiIdRequest;
import com.example.demo.dto.UpdateUpiIdRequest;
import com.example.demo.dto.UpiIdResponse;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.UpiId;
import com.example.demo.entity.UpiStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.BankAccountNotFoundException;
import com.example.demo.exception.UnauthorizedAccountAccessException;
import com.example.demo.exception.UpiIdNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UpiIdService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the UpiIdService interface.
 * 
 * Constructor Injection:
 * We use Constructor Injection (implicitly autowired in Spring 4.3+) for required dependencies. 
 * This ensures our service cannot be instantiated without its required dependencies, making it 
 * safer, preventing null references, and making it easier to unit test compared to Field Injection (@Autowired).
 * 
 * Business Logic vs Repository Logic:
 * Repositories only handle data access (fetching/saving from DB). 
 * This Service layer handles the *Business Logic*: checking if a user owns an account, 
 * enforcing the "only one primary UPI ID" rule, and generating unique handles. 
 * This separation of concerns keeps the repository lean and makes the system highly maintainable.
 * 
 * DTO Mapping:
 * We map Entities (like UpiId) to DTOs (UpiIdResponse) before returning them to the controller.
 * This hides internal database structure, prevents infinite recursion in JSON serialization,
 * and improves API security by avoiding over-exposure of internal IDs and timestamps.
 * 
 * Security checks & Ownership validation:
 * We always verify that the BankAccount or UpiId being accessed belongs to the currently 
 * authenticated user. This prevents IDOR (Insecure Direct Object Reference) attacks, where 
 * a user could otherwise manipulate another user's UPI IDs by guessing their primary keys.
 * 
 * Why helper methods improve maintainability:
 * Reusable logic (like getCurrentUser, generateUniqueUpiId, convertToResponseDTO) is extracted 
 * into private helper methods to avoid code duplication (DRY principle). This makes the core 
 * business methods cleaner, shorter, and easier to comprehend.
 * 
 * Why @Transactional is used:
 * Used on methods that modify data (create, update, delete, setPrimary). It ensures that all database operations 
 * within the method execute in a single atomic transaction. If any operation fails (e.g., throwing an exception), 
 * the entire transaction is rolled back safely, preventing partial updates and maintaining data integrity.
 */
@Service
public class UpiIdServiceImpl implements UpiIdService {

    private final UpiIdRepository upiIdRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public UpiIdServiceImpl(UpiIdRepository upiIdRepository, 
                            BankAccountRepository bankAccountRepository, 
                            UserRepository userRepository) {
        this.upiIdRepository = upiIdRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Helper method to get the currently authenticated user from SecurityContext.
     */
    private User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in the system."));
    }

    /**
     * Helper method to generate a unique UPI ID based on name and preferred handle.
     */
    private String generateUniqueUpiId(String fullName, String preferredHandle) {
        String[] parts = fullName.trim().split("\\s+");
        String firstName = parts[0].toLowerCase();
        String lastName = parts.length > 1 ? parts[parts.length - 1].toLowerCase() : "";
        
        String baseName = lastName.isEmpty() ? firstName : firstName + "." + lastName;
        String generatedId = baseName + "@" + preferredHandle;
        
        int counter = 123;
        while (upiIdRepository.existsByUpiId(generatedId)) {
            generatedId = baseName + counter + "@" + preferredHandle;
            counter++;
        }
        return generatedId;
    }

    /**
     * Helper method to safely convert Entity to DTO.
     */
    private UpiIdResponse convertToResponseDTO(UpiId upiId) {
        return UpiIdResponse.builder()
                .id(upiId.getId())
                .upiId(upiId.getUpiId())
                .status(upiId.getStatus())
                .isPrimary(upiId.isPrimary())
                .createdAt(upiId.getCreatedAt())
                .bankAccountNumber(upiId.getBankAccount().getAccountNumber())
                .build();
    }

    @Override
    @Transactional
    public UpiIdResponse createUpiId(CreateUpiIdRequest request) {
        User currentUser = getCurrentUser();
        
        BankAccount bankAccount = bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found"));
                
        if (!bankAccount.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccountAccessException("You do not own this bank account.");
        }
        
        String uniqueUpiIdStr = generateUniqueUpiId(currentUser.getName(), request.getPreferredHandle());
        
        List<UpiId> existingUpiIds = upiIdRepository.findByBankAccount(bankAccount);
        boolean isFirst = existingUpiIds.isEmpty();
        
        UpiId newUpiId = new UpiId();
        newUpiId.setUpiId(uniqueUpiIdStr);
        newUpiId.setBankAccount(bankAccount);
        newUpiId.setStatus(UpiStatus.ACTIVE);
        newUpiId.setPrimary(isFirst); // Business rule: automatically mark as Primary if first
        
        UpiId saved = upiIdRepository.save(newUpiId);
        return convertToResponseDTO(saved);
    }

    @Override
    public List<UpiIdResponse> getAllUpiIds() {
        User currentUser = getCurrentUser();
        
        List<BankAccount> userAccounts = bankAccountRepository.findAllByUser(currentUser);
        
        return userAccounts.stream()
                .flatMap(account -> upiIdRepository.findByBankAccount(account).stream())
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UpiIdResponse getUpiId(Long id) {
        User currentUser = getCurrentUser();
        
        UpiId upiId = upiIdRepository.findById(id)
                .orElseThrow(() -> new UpiIdNotFoundException("UPI ID not found"));
                
        if (!upiId.getBankAccount().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccountAccessException("You do not own this UPI ID.");
        }
        
        return convertToResponseDTO(upiId);
    }

    @Override
    @Transactional
    public UpiIdResponse updateUpiId(Long id, UpdateUpiIdRequest request) {
        User currentUser = getCurrentUser();
        
        UpiId upiId = upiIdRepository.findById(id)
                .orElseThrow(() -> new UpiIdNotFoundException("UPI ID not found"));
                
        BankAccount account = upiId.getBankAccount();
                
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccountAccessException("You do not own this UPI ID.");
        }
        
        upiId.setStatus(request.getStatus());
        
        if (request.getIsPrimary() && !upiId.isPrimary()) {
            // Rule: If another UPI ID becomes Primary, automatically remove Primary status from the previous one
            Optional<UpiId> existingPrimary = upiIdRepository.findByBankAccountAndIsPrimaryTrue(account);
            existingPrimary.ifPresent(primary -> {
                primary.setPrimary(false);
                upiIdRepository.save(primary);
            });
            upiId.setPrimary(true);
        } else if (!request.getIsPrimary() && upiId.isPrimary()) {
            upiId.setPrimary(false);
        }
        
        UpiId saved = upiIdRepository.save(upiId);
        return convertToResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteUpiId(Long id) {
        User currentUser = getCurrentUser();
        
        UpiId upiId = upiIdRepository.findById(id)
                .orElseThrow(() -> new UpiIdNotFoundException("UPI ID not found"));
                
        BankAccount account = upiId.getBankAccount();
                
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccountAccessException("You do not own this UPI ID.");
        }
        
        boolean wasPrimary = upiId.isPrimary();
        upiIdRepository.delete(upiId);
        upiIdRepository.flush(); // Ensure deletion happens before we try to find a remaining one
        
        // Rule: When deleting the current Primary UPI ID, automatically promote another existing UPI ID as Primary if available
        if (wasPrimary) {
            List<UpiId> remaining = upiIdRepository.findByBankAccount(account);
            if (!remaining.isEmpty()) {
                UpiId newPrimary = remaining.get(0);
                newPrimary.setPrimary(true);
                upiIdRepository.save(newPrimary);
            }
        }
    }

    @Override
    @Transactional
    public UpiIdResponse setPrimaryUpiId(Long id) {
        User currentUser = getCurrentUser();
        
        UpiId upiId = upiIdRepository.findById(id)
                .orElseThrow(() -> new UpiIdNotFoundException("UPI ID not found"));
                
        BankAccount account = upiId.getBankAccount();
                
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccountAccessException("You do not own this UPI ID.");
        }
        
        if (!upiId.isPrimary()) {
            Optional<UpiId> existingPrimary = upiIdRepository.findByBankAccountAndIsPrimaryTrue(account);
            existingPrimary.ifPresent(primary -> {
                primary.setPrimary(false);
                upiIdRepository.save(primary);
            });
            upiId.setPrimary(true);
            upiId = upiIdRepository.save(upiId);
        }
        
        return convertToResponseDTO(upiId);
    }
}
