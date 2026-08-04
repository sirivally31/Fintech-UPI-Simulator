package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnableUser() {
        User user = new User();
        user.setId(1L);
        user.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = adminUserService.enableUser(1L, "admin");

        assertTrue(result.getEnabled());
        verify(userRepository).save(user);
        verify(eventPublisher).publishAdminAction(any());
    }

    @Test
    void testLockUser() {
        User user = new User();
        user.setId(1L);
        user.setLocked(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = adminUserService.lockUser(1L, "admin");

        assertTrue(result.getLocked());
        verify(userRepository).save(user);
        verify(eventPublisher).publishAdminAction(any());
    }
}
