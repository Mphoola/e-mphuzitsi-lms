package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.user.UserRoleRequest;
import com.mphoola.e_empuzitsi.dto.user.UserRoleResponse;
import com.mphoola.e_empuzitsi.dto.user.UserPermissionRequest;
import com.mphoola.e_empuzitsi.dto.user.UserPermissionResponse;
import com.mphoola.e_empuzitsi.dto.user.UserAccessResponse;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.dto.role.RoleResponse;
import com.mphoola.e_empuzitsi.dto.role.PermissionResponse;
import com.mphoola.e_empuzitsi.entity.*;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.ValidationException;
import com.mphoola.e_empuzitsi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAccessManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final ActivityLogService activityLogService;

    public UserAccessManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserPermissionRepository userPermissionRepository,
            ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.activityLogService = activityLogService;
    }

    // ==================== USER ROLE MANAGEMENT ====================

    public UserRoleResponse assignRoleToUser(Long userId, Long roleId, String reason) {
        User user = findUserById(userId);
        Role role = findRoleById(roleId);

        // Check if user already has this role
        boolean hasRole = user.getUserRoles() != null && 
                user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getId().equals(roleId));

        if (hasRole) {
            throw new ValidationException("User already has this role");
        }

        // Create the user role association
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();

        // Initialize userRoles collection if needed
        if (user.getUserRoles() == null) {
            user.setUserRoles(new HashSet<>());
        }
        user.getUserRoles().add(userRole);

        // Save the user
        userRepository.save(user);

        // Log the activity
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("user_role_assigned")
                .description(String.format("Role '%s' assigned to user '%s'", role.getName(), user.getName()))
                .on(user)
                .withProperties(java.util.Map.of(
                        "role_id", roleId,
                        "role_name", role.getName(),
                        "reason", reason != null ? reason : "No reason provided"
                ))
                .log();

        return buildUserRoleResponse(userRole);
    }

    public void revokeRoleFromUser(Long userId, Long roleId) {
        User user = findUserById(userId);
        Role role = findRoleById(roleId);

        if (user.getUserRoles() == null) {
            throw new ResourceNotFoundException("User has no roles assigned");
        }

        UserRole userRoleToRemove = user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getId().equals(roleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User does not have this role"));

        user.getUserRoles().remove(userRoleToRemove);
        userRepository.save(user);

        // Log the activity
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("user_role_revoked")
                .description(String.format("Role '%s' revoked from user '%s'", role.getName(), user.getName()))
                .on(user)
                .withProperties(java.util.Map.of(
                        "role_id", roleId,
                        "role_name", role.getName()
                ))
                .log();
    }

    public List<UserRoleResponse> getUserRoles(Long userId) {
        User user = findUserById(userId);
        
        if (user.getUserRoles() == null) {
            return new ArrayList<>();
        }

        return user.getUserRoles().stream()
                .map(this::buildUserRoleResponse)
                .collect(Collectors.toList());
    }

    // ==================== USER PERMISSION MANAGEMENT ====================

    public UserPermissionResponse assignPermissionToUser(Long userId, Long permissionId, String reason) {
        User user = findUserById(userId);
        Permission permission = findPermissionById(permissionId);

        // Check if user already has this permission directly
        if (userPermissionRepository.existsByUserIdAndPermissionId(userId, permissionId)) {
            throw new ValidationException("User already has this permission assigned directly");
        }

        // Create direct permission assignment
        UserPermission userPermission = UserPermission.builder()
                .user(user)
                .permission(permission)
                .build();

        userPermissionRepository.save(userPermission);

        // Log the activity
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("user_permission_assigned")
                .description(String.format("Permission '%s' assigned directly to user '%s'", permission.getName(), user.getName()))
                .on(user)
                .withProperties(java.util.Map.of(
                        "permission_id", permissionId,
                        "permission_name", permission.getName(),
                        "reason", reason != null ? reason : "No reason provided"
                ))
                .log();

        return buildUserPermissionResponse(userPermission, "DIRECT");
    }

    public void revokePermissionFromUser(Long userId, Long permissionId) {
        User user = findUserById(userId);
        Permission permission = findPermissionById(permissionId);

        UserPermission userPermission = userPermissionRepository.findByUserIdAndPermissionId(userId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have this permission assigned directly"));

        userPermissionRepository.delete(userPermission);

        // Log the activity
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("user_permission_revoked")
                .description(String.format("Permission '%s' revoked from user '%s'", permission.getName(), user.getName()))
                .on(user)
                .withProperties(java.util.Map.of(
                        "permission_id", permissionId,
                        "permission_name", permission.getName()
                ))
                .log();
    }

    public List<UserPermissionResponse> getUserPermissions(Long userId) {
        User user = findUserById(userId);
        List<UserPermissionResponse> permissions = new ArrayList<>();

        // Get direct permissions
        List<UserPermission> directPermissions = userPermissionRepository.findByUserId(userId);
        permissions.addAll(directPermissions.stream()
                .map(up -> buildUserPermissionResponse(up, "DIRECT"))
                .collect(Collectors.toList()));

        // Get permissions from roles
        if (user.getUserRoles() != null) {
            for (UserRole userRole : user.getUserRoles()) {
                if (userRole.getRole().getPermissions() != null) {
                    for (Permission permission : userRole.getRole().getPermissions()) {
                        UserPermissionResponse permissionResponse = UserPermissionResponse.builder()
                                .userId(userId)
                                .userName(user.getName())
                                .userEmail(user.getEmail())
                                .permissionId(permission.getId())
                                .permissionName(permission.getName())
                                .assignedAt(userRole.getCreatedAt())
                                .createdAt(permission.getCreatedAt())
                                .updatedAt(permission.getUpdatedAt())
                                .source("ROLE (" + userRole.getRole().getName() + ")")
                                .build();
                        permissions.add(permissionResponse);
                    }
                }
            }
        }

        return permissions;
    }

    // ==================== USER ACCESS OVERVIEW ====================

    public UserAccessResponse getUserAccess(Long userId) {
        User user = findUserById(userId);
        
        List<UserRoleResponse> roles = getUserRoles(userId);
        List<UserPermissionResponse> permissions = getUserPermissions(userId);
        
        // Get effective permissions (unique list)
        Set<String> effectivePermissions = new HashSet<>();
        permissions.forEach(p -> effectivePermissions.add(p.getPermissionName()));
        
        return UserAccessResponse.builder()
                .userId(userId)
                .userName(user.getName())
                .userEmail(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .effectivePermissions(new ArrayList<>(effectivePermissions))
                .build();
    }

    // ==================== HELPER METHODS ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }

    private Permission findPermissionById(Long permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));
    }

    private UserRoleResponse buildUserRoleResponse(UserRole userRole) {
        return UserRoleResponse.builder()
                .userId(userRole.getUser().getId())
                .userName(userRole.getUser().getName())
                .userEmail(userRole.getUser().getEmail())
                .roleId(userRole.getRole().getId())
                .roleName(userRole.getRole().getName())
                .assignedAt(userRole.getCreatedAt())
                .createdAt(userRole.getCreatedAt())
                .updatedAt(userRole.getUpdatedAt())
                .build();
    }

    private UserPermissionResponse buildUserPermissionResponse(UserPermission userPermission, String source) {
        return UserPermissionResponse.builder()
                .userId(userPermission.getUser().getId())
                .userName(userPermission.getUser().getName())
                .userEmail(userPermission.getUser().getEmail())
                .permissionId(userPermission.getPermission().getId())
                .permissionName(userPermission.getPermission().getName())
                .assignedAt(userPermission.getCreatedAt())
                .createdAt(userPermission.getCreatedAt())
                .updatedAt(userPermission.getUpdatedAt())
                .source(source)
                .build();
    }
}
