package com.example.demo.service;

import com.example.demo.entity.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface AdminMerchantService {

    Page<Merchant> getAllMerchants(Pageable pageable);

    Merchant approveMerchant(UUID merchantId, String adminUsername);

    Merchant rejectMerchant(UUID merchantId, String reason, String adminUsername);

    Merchant suspendMerchant(UUID merchantId, String reason, String adminUsername);

    Merchant activateMerchant(UUID merchantId, String adminUsername);
}
