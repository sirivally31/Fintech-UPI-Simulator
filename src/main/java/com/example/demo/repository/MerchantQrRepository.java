package com.example.demo.repository;

import com.example.demo.entity.MerchantQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for MerchantQr entity.
 */
@Repository
public interface MerchantQrRepository extends JpaRepository<MerchantQr, UUID> {

    Optional<MerchantQr> findByQrToken(String qrToken);

    boolean existsByQrToken(String qrToken);
}
