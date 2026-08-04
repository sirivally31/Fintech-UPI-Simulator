package com.example.demo.service.impl;

import com.example.demo.entity.Merchant;
import com.example.demo.events.AdminActionEvent;
import com.example.demo.events.MerchantApprovedEvent;
import com.example.demo.events.MerchantBlockedEvent;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.MerchantRepository;
import com.example.demo.service.AdminMerchantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AdminMerchantServiceImpl implements AdminMerchantService {

    private final MerchantRepository merchantRepository;
    private final EventPublisher eventPublisher;

    public AdminMerchantServiceImpl(MerchantRepository merchantRepository, EventPublisher eventPublisher) {
        this.merchantRepository = merchantRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Merchant> getAllMerchants(Pageable pageable) {
        return merchantRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Merchant approveMerchant(UUID merchantId, String adminUsername) {
        Merchant merchant = getMerchant(merchantId);
        merchant.setApproved(true);
        merchant.setActive(true);
        Merchant saved = merchantRepository.save(merchant);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "MERCHANT_APPROVED", "Merchant ID: " + merchantId));
        eventPublisher.publishMerchantApproved(new MerchantApprovedEvent(merchant.getMerchantCode(), adminUsername));
        return saved;
    }

    @Override
    @Transactional
    public Merchant rejectMerchant(UUID merchantId, String reason, String adminUsername) {
        Merchant merchant = getMerchant(merchantId);
        merchant.setApproved(false);
        merchant.setActive(false);
        Merchant saved = merchantRepository.save(merchant);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "MERCHANT_REJECTED", "Merchant ID: " + merchantId + ", Reason: " + reason));
        return saved;
    }

    @Override
    @Transactional
    public Merchant suspendMerchant(UUID merchantId, String reason, String adminUsername) {
        Merchant merchant = getMerchant(merchantId);
        merchant.setSuspended(true);
        merchant.setActive(false);
        Merchant saved = merchantRepository.save(merchant);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "MERCHANT_SUSPENDED", "Merchant ID: " + merchantId + ", Reason: " + reason));
        eventPublisher.publishMerchantBlocked(new MerchantBlockedEvent(merchant.getMerchantCode(), adminUsername, reason));
        return saved;
    }

    @Override
    @Transactional
    public Merchant activateMerchant(UUID merchantId, String adminUsername) {
        Merchant merchant = getMerchant(merchantId);
        merchant.setSuspended(false);
        merchant.setActive(true);
        Merchant saved = merchantRepository.save(merchant);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "MERCHANT_ACTIVATED", "Merchant ID: " + merchantId));
        return saved;
    }

    private Merchant getMerchant(UUID merchantId) {
        return merchantRepository.findById(merchantId).orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId));
    }
}
