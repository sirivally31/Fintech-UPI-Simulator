package com.example.demo.repository;

import com.example.demo.entity.Beneficiary;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Beneficiary entity.
 */
@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByOwner(User owner);

    Optional<Beneficiary> findByIdAndOwner(UUID id, User owner);

    Optional<Beneficiary> findByOwnerAndBeneficiaryUpiId(User owner, String beneficiaryUpiId);

    boolean existsByOwnerAndBeneficiaryUpiId(User owner, String beneficiaryUpiId);

    List<Beneficiary> findByOwnerAndFavouriteTrue(User owner);

    List<Beneficiary> findByOwnerAndBeneficiaryNameContainingIgnoreCaseOrNicknameContainingIgnoreCaseOrBeneficiaryUpiIdContainingIgnoreCase(
            User owner, String beneficiaryName, String nickname, String beneficiaryUpiId);

    long countByOwner(User owner);
}
