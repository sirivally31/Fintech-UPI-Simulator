package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.events.AdminActionEvent;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(UserRepository userRepository, EventPublisher eventPublisher, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCase(query, pageable);
    }

    @Override
    @Transactional
    public User enableUser(Long userId, String adminUsername) {
        User user = getUser(userId);
        user.setEnabled(true);
        User saved = userRepository.save(user);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "USER_ENABLED", "User ID: " + userId));
        return saved;
    }

    @Override
    @Transactional
    public User disableUser(Long userId, String adminUsername) {
        User user = getUser(userId);
        user.setEnabled(false);
        User saved = userRepository.save(user);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "USER_DISABLED", "User ID: " + userId));
        return saved;
    }

    @Override
    @Transactional
    public User lockUser(Long userId, String adminUsername) {
        User user = getUser(userId);
        user.setLocked(true);
        User saved = userRepository.save(user);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "USER_LOCKED", "User ID: " + userId));
        return saved;
    }

    @Override
    @Transactional
    public User unlockUser(Long userId, String adminUsername) {
        User user = getUser(userId);
        user.setLocked(false);
        User saved = userRepository.save(user);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "USER_UNLOCKED", "User ID: " + userId));
        return saved;
    }

    @Override
    @Transactional
    public User resetUserPin(Long userId, String newPin, String adminUsername) {
        User user = getUser(userId);
        user.setPin(passwordEncoder.encode(newPin));
        User saved = userRepository.save(user);
        eventPublisher.publishAdminAction(new AdminActionEvent(adminUsername, "PIN_RESET", "User ID: " + userId));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserProfile(Long userId) {
        return getUser(userId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
