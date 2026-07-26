package com.example.demo.service.impl;

import com.example.demo.dto.AddBeneficiaryRequest;
import com.example.demo.dto.BeneficiaryListResponse;
import com.example.demo.dto.BeneficiaryResponse;
import com.example.demo.entity.Beneficiary;
import com.example.demo.entity.User;
import com.example.demo.exception.BeneficiaryAlreadyExistsException;
import com.example.demo.exception.BeneficiaryValidationException;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.BeneficiaryRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceImplTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UpiIdRepository upiIdRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private BeneficiaryServiceImpl beneficiaryService;

    private User owner;
    private Beneficiary beneficiary;
    private UUID beneficiaryId;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUpiId("john@upi");

        beneficiaryId = UUID.randomUUID();
        beneficiary = new Beneficiary(beneficiaryId, owner, "Alice Smith", "alice@upi", "Alice Work", false, true, LocalDateTime.now(), LocalDateTime.now());

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john@upi");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Add Beneficiary - Successful Addition")
    void testAddBeneficiary_Success() {
        AddBeneficiaryRequest request = new AddBeneficiaryRequest("Alice Smith", "alice@upi", "Alice Work", false);

        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.existsByOwnerAndBeneficiaryUpiId(owner, "alice@upi")).thenReturn(false);
        when(upiIdRepository.existsByUpiId("alice@upi")).thenReturn(true);
        when(beneficiaryRepository.countByOwner(owner)).thenReturn(2L);
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(i -> {
            Beneficiary b = i.getArgument(0);
            b.setId(beneficiaryId);
            return b;
        });

        BeneficiaryResponse response = beneficiaryService.addBeneficiary(request);

        assertNotNull(response);
        assertEquals("alice@upi", response.getBeneficiaryUpiId());
        assertEquals("Alice Smith", response.getBeneficiaryName());

        verify(redisCacheService).delete("beneficiaries:owner:1");
        verify(outboxService).saveOutboxEvent(any(), eq("BENEFICIARY"), anyLong(), eq("BENEFICIARY_ADDED"), anyString(), any());
        verify(eventPublisher).publishBeneficiaryAdded(any());
    }

    @Test
    @DisplayName("Add Beneficiary - Self Addition Throws Exception")
    void testAddBeneficiary_SelfAddition_ThrowsException() {
        AddBeneficiaryRequest request = new AddBeneficiaryRequest("John Self", "john@upi", "My Self", false);

        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));

        BeneficiaryValidationException ex = assertThrows(BeneficiaryValidationException.class, () -> beneficiaryService.addBeneficiary(request));
        assertTrue(ex.getMessage().contains("own UPI ID"));

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add Beneficiary - Duplicate Beneficiary Throws Exception")
    void testAddBeneficiary_Duplicate_ThrowsException() {
        AddBeneficiaryRequest request = new AddBeneficiaryRequest("Alice Smith", "alice@upi", "Alice Work", false);

        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.existsByOwnerAndBeneficiaryUpiId(owner, "alice@upi")).thenReturn(true);

        assertThrows(BeneficiaryAlreadyExistsException.class, () -> beneficiaryService.addBeneficiary(request));
        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Mark Favourite - Success")
    void testMarkFavourite_Success() {
        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.findByIdAndOwner(beneficiaryId, owner)).thenReturn(Optional.of(beneficiary));
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(beneficiary);

        BeneficiaryResponse response = beneficiaryService.markFavourite(beneficiaryId, true);

        assertNotNull(response);
        assertTrue(beneficiary.getFavourite());
        verify(redisCacheService).delete("beneficiaries:owner:1");
    }

    @Test
    @DisplayName("Delete Beneficiary - Success")
    void testDeleteBeneficiary_Success() {
        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.findByIdAndOwner(beneficiaryId, owner)).thenReturn(Optional.of(beneficiary));

        beneficiaryService.deleteBeneficiary(beneficiaryId);

        verify(beneficiaryRepository).delete(beneficiary);
        verify(redisCacheService).delete("beneficiaries:owner:1");
        verify(redisCacheService).delete("beneficiary:id:" + beneficiaryId);
        verify(eventPublisher).publishBeneficiaryDeleted(any());
    }

    @Test
    @DisplayName("Search Beneficiaries - Success")
    void testSearchBeneficiaries_Success() {
        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.findByOwnerAndBeneficiaryNameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrBeneficiaryUpiIdContainingIgnoreCase(
                owner, "Alice", "Alice", "Alice")).thenReturn(List.of(beneficiary));

        List<BeneficiaryResponse> results = beneficiaryService.searchBeneficiaries("Alice");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("alice@upi", results.get(0).getBeneficiaryUpiId());
    }
}
