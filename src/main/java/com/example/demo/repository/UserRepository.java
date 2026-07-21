package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUpiId(String upiId);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByUpiId(String upiId);
    
    boolean existsByPhoneNumber(String phoneNumber);
}
