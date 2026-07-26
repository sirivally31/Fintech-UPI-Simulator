package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.Beneficiary;
import com.example.demo.entity.User;
import com.example.demo.events.BeneficiaryAddedEvent;
import com.example.demo.events.BeneficiaryDeletedEvent;
import com.example.demo.events.BeneficiaryUpdatedEvent;
import com.example.demo.exception.*;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.BeneficiaryRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BeneficiaryService;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of BeneficiaryService providing CRUD operations, validation checks,
 * Redis caching, transactional outbox pattern persistence, and Kafka event streaming.
 */
@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private static final Logger log = LoggerFactory.getLogger(BeneficiaryServiceImpl.class);
    private static final String CACHE_OWNER_PREFIX = "beneficiaries:owner:";
    private static final String CACHE_ID_PREFIX = "beneficiary:id:";

    @Value("${app.beneficiary.max-limit:50}")
    private long maxBeneficiaryLimit = 50;

    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final UpiIdRepository upiIdRepository;
    private final RedisCacheService redisCacheService;
    private final OutboxService outboxService;
    private final EventPublisher eventPublisher;

    public BeneficiaryServiceImpl(BeneficiaryRepository beneficiaryRepository,
                                  UserRepository userRepository,
                                  UpiIdRepository upiIdRepository,
                                  RedisCacheService redisCacheService,
                                  OutboxService outboxService,
                                  EventPublisher eventPublisher) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
        this.upiIdRepository = upiIdRepository;
        this.redisCacheService = redisCacheService;
        this.outboxService = outboxService;
        this.eventPublisher = eventPublisher;
    }

    private User getCurrentOwner() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUpiId(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public BeneficiaryResponse addBeneficiary(AddBeneficiaryRequest request) {
        User owner = getCurrentOwner();
        log.info("Adding new beneficiary [{}] for owner [{}]", request.getBeneficiaryUpiId(), owner.getUpiId());

        // 1. Prevent self-addition
        if (owner.getUpiId().equalsIgnoreCase(request.getBeneficiaryUpiId())) {
            throw new BeneficiaryValidationException("Cannot add your own UPI ID as a beneficiary.");
        }

        // 2. Prevent duplicates
        if (beneficiaryRepository.existsByOwnerAndBeneficiaryUpiId(owner, request.getBeneficiaryUpiId())) {
            throw new BeneficiaryAlreadyExistsException("Beneficiary with UPI ID '" + request.getBeneficiaryUpiId() + "' already exists for this owner.");
        }

        // 3. Verify target UPI ID existence
        boolean upiExists = upiIdRepository.existsByUpiId(request.getBeneficiaryUpiId());
        if (!upiExists) {
            throw new BeneficiaryValidationException("Beneficiary UPI ID '" + request.getBeneficiaryUpiId() + "' is not registered in the system.");
        }

        // 4. Maximum limit check
        long effectiveLimit = maxBeneficiaryLimit > 0 ? maxBeneficiaryLimit : 50;
        if (beneficiaryRepository.countByOwner(owner) >= effectiveLimit) {
            throw new BeneficiaryValidationException("Maximum beneficiary limit of " + effectiveLimit + " reached.");
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setOwner(owner);
        beneficiary.setBeneficiaryName(request.getBeneficiaryName());
        beneficiary.setBeneficiaryUpiId(request.getBeneficiaryUpiId());
        beneficiary.setNickname(request.getNickname());
        beneficiary.setFavourite(Boolean.TRUE.equals(request.getFavourite()));
        beneficiary.setVerified(true); // Automatically marked verified if exists in system

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        BeneficiaryResponse response = mapToResponse(saved);

        // 5. Invalidate & Update Caches
        evictOwnerCache(owner.getId());
        redisCacheService.save(CACHE_ID_PREFIX + saved.getId(), response, 1, TimeUnit.HOURS);

        // 6. Save Outbox Event & Publish Kafka Event
        String correlationId = UUID.randomUUID().toString();
        BeneficiaryAddedEvent event = BeneficiaryAddedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("BENEFICIARY_ADDED")
                .correlationId(correlationId)
                .beneficiaryId(saved.getId())
                .ownerUpiId(owner.getUpiId())
                .beneficiaryName(saved.getBeneficiaryName())
                .beneficiaryUpiId(saved.getBeneficiaryUpiId())
                .nickname(saved.getNickname())
                .favourite(saved.getFavourite())
                .build();

        outboxService.saveOutboxEvent(event.getEventId(), "BENEFICIARY", (long) Math.abs(saved.getId().hashCode()), "BENEFICIARY_ADDED", correlationId, event);
        eventPublisher.publishBeneficiaryAdded(event);

        log.info("Successfully added beneficiary ID [{}] for owner [{}]", saved.getId(), owner.getUpiId());
        return response;
    }

    @Override
    @Transactional
    public BeneficiaryResponse updateBeneficiary(UUID id, UpdateBeneficiaryRequest request) {
        User owner = getCurrentOwner();
        log.info("Updating beneficiary ID [{}] for owner [{}]", id, owner.getUpiId());

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found with ID: " + id));

        if (request.getBeneficiaryName() != null && !request.getBeneficiaryName().isBlank()) {
            beneficiary.setBeneficiaryName(request.getBeneficiaryName());
        }
        if (request.getNickname() != null) {
            beneficiary.setNickname(request.getNickname());
        }
        if (request.getFavourite() != null) {
            beneficiary.setFavourite(request.getFavourite());
        }

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        BeneficiaryResponse response = mapToResponse(updated);

        evictOwnerCache(owner.getId());
        redisCacheService.save(CACHE_ID_PREFIX + id, response, 1, TimeUnit.HOURS);

        String correlationId = UUID.randomUUID().toString();
        BeneficiaryUpdatedEvent event = BeneficiaryUpdatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("BENEFICIARY_UPDATED")
                .correlationId(correlationId)
                .beneficiaryId(updated.getId())
                .ownerUpiId(owner.getUpiId())
                .beneficiaryName(updated.getBeneficiaryName())
                .beneficiaryUpiId(updated.getBeneficiaryUpiId())
                .nickname(updated.getNickname())
                .favourite(updated.getFavourite())
                .verified(updated.getVerified())
                .build();

        outboxService.saveOutboxEvent(event.getEventId(), "BENEFICIARY", (long) Math.abs(updated.getId().hashCode()), "BENEFICIARY_UPDATED", correlationId, event);
        eventPublisher.publishBeneficiaryUpdated(event);

        return response;
    }

    @Override
    @Transactional
    public void deleteBeneficiary(UUID id) {
        User owner = getCurrentOwner();
        log.info("Deleting beneficiary ID [{}] for owner [{}]", id, owner.getUpiId());

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found with ID: " + id));

        beneficiaryRepository.delete(beneficiary);

        evictOwnerCache(owner.getId());
        redisCacheService.delete(CACHE_ID_PREFIX + id);

        String correlationId = UUID.randomUUID().toString();
        BeneficiaryDeletedEvent event = BeneficiaryDeletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("BENEFICIARY_DELETED")
                .correlationId(correlationId)
                .beneficiaryId(id)
                .ownerUpiId(owner.getUpiId())
                .beneficiaryUpiId(beneficiary.getBeneficiaryUpiId())
                .build();

        outboxService.saveOutboxEvent(event.getEventId(), "BENEFICIARY", (long) Math.abs(id.hashCode()), "BENEFICIARY_DELETED", correlationId, event);
        eventPublisher.publishBeneficiaryDeleted(event);

        log.info("Successfully deleted beneficiary ID [{}]", id);
    }

    @Override
    @Transactional
    public BeneficiaryResponse markFavourite(UUID id, boolean favourite) {
        User owner = getCurrentOwner();
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found with ID: " + id));

        beneficiary.setFavourite(favourite);
        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        BeneficiaryResponse response = mapToResponse(updated);

        evictOwnerCache(owner.getId());
        redisCacheService.save(CACHE_ID_PREFIX + id, response, 1, TimeUnit.HOURS);
        return response;
    }

    @Override
    @Transactional
    public BeneficiaryResponse verifyBeneficiary(UUID id) {
        User owner = getCurrentOwner();
        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found with ID: " + id));

        boolean exists = upiIdRepository.existsByUpiId(beneficiary.getBeneficiaryUpiId());
        beneficiary.setVerified(exists);

        Beneficiary updated = beneficiaryRepository.save(beneficiary);
        BeneficiaryResponse response = mapToResponse(updated);

        evictOwnerCache(owner.getId());
        redisCacheService.save(CACHE_ID_PREFIX + id, response, 1, TimeUnit.HOURS);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryResponse getBeneficiary(UUID id) {
        User owner = getCurrentOwner();
        String cacheKey = CACHE_ID_PREFIX + id;
        BeneficiaryResponse cached = redisCacheService.find(cacheKey, BeneficiaryResponse.class);
        if (cached != null) {
            return cached;
        }

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found with ID: " + id));

        BeneficiaryResponse response = mapToResponse(beneficiary);
        redisCacheService.save(cacheKey, response, 1, TimeUnit.HOURS);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryListResponse getAllBeneficiaries() {
        User owner = getCurrentOwner();
        List<BeneficiaryResponse> list = beneficiaryRepository.findByOwner(owner).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new BeneficiaryListResponse(list, list.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> searchBeneficiaries(String query) {
        User owner = getCurrentOwner();
        if (query == null || query.isBlank()) {
            return getAllBeneficiaries().getBeneficiaries();
        }

        String q = query.trim();
        return beneficiaryRepository.findByOwnerAndBeneficiaryNameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrBeneficiaryUpiIdContainingIgnoreCase(
                owner, q, q, q).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void evictOwnerCache(Long ownerId) {
        redisCacheService.delete(CACHE_OWNER_PREFIX + ownerId);
    }

    private BeneficiaryResponse mapToResponse(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getOwner().getUpiId(),
                beneficiary.getBeneficiaryName(),
                beneficiary.getBeneficiaryUpiId(),
                beneficiary.getNickname(),
                beneficiary.getFavourite(),
                beneficiary.getVerified(),
                beneficiary.getCreatedAt(),
                beneficiary.getUpdatedAt()
        );
    }
}
