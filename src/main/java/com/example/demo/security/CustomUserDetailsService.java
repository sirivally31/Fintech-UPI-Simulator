package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String upiId) throws UsernameNotFoundException {
        User user = userRepository.findByUpiId(upiId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with UPI ID: " + upiId));

        return new org.springframework.security.core.userdetails.User(
                user.getUpiId(),
                user.getPin(),
                Collections.emptyList()
        );
    }
}
