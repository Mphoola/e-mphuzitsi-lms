package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.PermissionResponse;
import com.mphoola.e_empuzitsi.dto.RoleRequest;
import com.mphoola.e_empuzitsi.dto.RoleResponse;
import com.mphoola.e_empuzitsi.dto.UserResponse;
import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.RoleInUseException;
import com.mphoola.e_empuzitsi.repository.PermissionRepository;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }
    
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new ResourceConflictException("Role already exists with name: " + request.getName());
        }
        
        Set<Permission> permissions = validateAndFetchPermissions(request.getPermissionIds());
        
        Role role = Role.builder()
                .name(request.getName())
                .permissions(permissions)
                .build();
        
        Role savedRole = roleRepository.save(role);
        
        return mapToRoleResponseWithoutPermissions(savedRole);
    }
    
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        if (!role.getName().equals(request.getName()) && 
            roleRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ResourceConflictException("Role already exists with name: " + request.getName());
        }
        
        Set<Permission> permissions = validateAndFetchPermissions(request.getPermissionIds());
        
        role.setName(request.getName());
        role.setPermissions(permissions);
        
        Role updatedRole = roleRepository.save(role);
        
        return mapToRoleResponseWithoutPermissions(updatedRole);
    }
    
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        long userCount = roleRepository.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new RoleInUseException("Cannot delete role '" + role.getName() + 
                    "' as it is assigned to " + userCount + " user(s)");
        }
        
        roleRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        return mapToRoleResponseWithDetails(role);
    }
    
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::mapToRoleResponseWithCounts)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        
        return mapToRoleResponseWithoutPermissions(role);
    }
    
    private Set<Permission> validateAndFetchPermissions(Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<Permission> permissions = new HashSet<>();
        for (Long permissionId : permissionIds) {
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Permission not found with id: " + permissionId));
            permissions.add(permission);
        }
        
        return permissions;
    }
    
    private RoleResponse mapToRoleResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions() != null 
                ? role.getPermissions().stream()
                        .map(this::mapToPermissionResponse)
                        .collect(Collectors.toSet())
                : new HashSet<>();
        
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(permissionResponses)
                .users(new ArrayList<>())
                .userCount(0L)
                .permissionCount((long) permissionResponses.size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
    
    private RoleResponse mapToRoleResponseWithoutPermissions(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(new HashSet<>())
                .users(new ArrayList<>())
                .userCount(0L)
                .permissionCount(0L)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
    
    private RoleResponse mapToRoleResponseWithCounts(Role role) {
        long userCount = roleRepository.countUsersByRoleId(role.getId());
        long permissionCount = role.getPermissions() != null ? role.getPermissions().size() : 0;
        
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(new HashSet<>()) // Empty for list view
                .users(new ArrayList<>()) // Empty for list view
                .userCount(userCount)
                .permissionCount(permissionCount)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
    
    private RoleResponse mapToRoleResponseWithDetails(Role role) {
        // Get permissions
        Set<PermissionResponse> permissionResponses = role.getPermissions() != null 
                ? role.getPermissions().stream()
                        .map(this::mapToPermissionResponse)
                        .collect(Collectors.toSet())
                : new HashSet<>();
        
        // Get users with this role
        List<User> users = roleRepository.findUsersByRoleId(role.getId());
        List<UserResponse> userResponses = users.stream()
                .map(this::mapToSimpleUserResponse)
                .collect(Collectors.toList());
        
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(permissionResponses)
                .users(userResponses)
                .userCount((long) userResponses.size())
                .permissionCount((long) permissionResponses.size())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
    
    private PermissionResponse mapToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
    
    private UserResponse mapToSimpleUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(new HashSet<>()) // Empty for role details view to avoid recursion
                .permissions(new HashSet<>()) // Empty for role details view to avoid recursion
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
