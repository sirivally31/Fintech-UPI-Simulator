package com.example.demo.service.impl;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.events.RoleAssignedEvent;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.RoleManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class RoleManagementServiceImpl implements RoleManagementService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public RoleManagementServiceImpl(RoleRepository roleRepository, UserRepository userRepository, EventPublisher eventPublisher) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Role createRole(String roleName, List<String> permissions, String adminUsername) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new IllegalArgumentException("Role already exists: " + roleName);
        }
        Role role = new Role(roleName);
        if (permissions != null) {
            role.setPermissions(new HashSet<>(permissions));
        }
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        
        user.getRoles().add(role);
        userRepository.save(user);
        
        eventPublisher.publishRoleAssigned(new RoleAssignedEvent(adminUsername, user.getUpiId(), role.getName()));
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
