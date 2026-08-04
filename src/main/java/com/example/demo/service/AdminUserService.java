package com.example.demo.service;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<User> getAllUsers(Pageable pageable);

    Page<User> searchUsers(String query, Pageable pageable);

    User enableUser(Long userId, String adminUsername);

    User disableUser(Long userId, String adminUsername);

    User lockUser(Long userId, String adminUsername);

    User unlockUser(Long userId, String adminUsername);

    User resetUserPin(Long userId, String newPin, String adminUsername);

    User getUserProfile(Long userId);
}
