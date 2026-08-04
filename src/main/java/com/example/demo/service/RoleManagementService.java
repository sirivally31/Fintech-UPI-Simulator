package com.example.demo.service;

import com.example.demo.entity.Role;
import java.util.List;

public interface RoleManagementService {

    Role createRole(String roleName, List<String> permissions, String adminUsername);

    void assignRoleToUser(Long userId, Long roleId, String adminUsername);

    void removeRoleFromUser(Long userId, Long roleId, String adminUsername);

    List<Role> getAllRoles();
}
