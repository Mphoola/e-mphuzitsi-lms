package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.user.UserRoleRequest;
import com.mphoola.e_empuzitsi.dto.user.UserRoleResponse;
import com.mphoola.e_empuzitsi.dto.user.UserPermissionRequest;
import com.mphoola.e_empuzitsi.dto.user.UserPermissionResponse;
import com.mphoola.e_empuzitsi.dto.user.UserAccessResponse;
import com.mphoola.e_empuzitsi.service.UserAccessManagementService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Access Management", description = "Manage user roles and permissions")
public class UserAccessManagementController {

    private final UserAccessManagementService userAccessManagementService;

    public UserAccessManagementController(UserAccessManagementService userAccessManagementService) {
        this.userAccessManagementService = userAccessManagementService;
    }

    // ==================== USER ROLE MANAGEMENT ====================

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('assign_user_role')")
    @Operation(summary = "Assign role to user", description = "Assign a role to a specific user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role assigned successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or role not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already has this role")
    public ResponseEntity<Map<String, Object>> assignRoleToUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserRoleRequest request) {
        
        UserRoleResponse response = userAccessManagementService.assignRoleToUser(
                userId, request.getRoleId(), request.getReason());
        return ApiResponse.created(response);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('revoke_user_role')")
    @Operation(summary = "Revoke role from user", description = "Remove a role from a specific user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Role revoked successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User, role not found or user doesn't have this role")
    public ResponseEntity<Map<String, Object>> revokeRoleFromUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Role ID") @PathVariable Long roleId) {
        
        userAccessManagementService.revokeRoleFromUser(userId, roleId);
        return ApiResponse.success("Role revoked successfully");
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('list_user_roles')")
    @Operation(summary = "Get user roles", description = "Get all roles assigned to a specific user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User roles retrieved successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Map<String, Object>> getUserRoles(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        List<UserRoleResponse> roles = userAccessManagementService.getUserRoles(userId);
        return ApiResponse.success("User roles retrieved successfully", roles);
    }

    // ==================== USER PERMISSION MANAGEMENT ====================

    @PostMapping("/{userId}/permissions")
    @PreAuthorize("hasAuthority('assign_user_permission')")
    @Operation(summary = "Assign permission to user", description = "Assign a direct permission to a specific user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Permission assigned successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or permission not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already has this permission")
    public ResponseEntity<Map<String, Object>> assignPermissionToUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserPermissionRequest request) {
        
        UserPermissionResponse response = userAccessManagementService.assignPermissionToUser(
                userId, request.getPermissionId(), request.getReason());
        return ApiResponse.created(response);
    }

    @DeleteMapping("/{userId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('revoke_user_permission')")
    @Operation(summary = "Revoke permission from user", description = "Remove a direct permission from a specific user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Permission revoked successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User, permission not found or user doesn't have this permission")
    public ResponseEntity<Map<String, Object>> revokePermissionFromUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Permission ID") @PathVariable Long permissionId) {
        
        userAccessManagementService.revokePermissionFromUser(userId, permissionId);
        return ApiResponse.success("Permission revoked successfully");
    }

    @GetMapping("/{userId}/permissions")
    @PreAuthorize("hasAuthority('list_user_permissions')")
    @Operation(summary = "Get user permissions", description = "Get all permissions for a specific user (both direct and from roles)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User permissions retrieved successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Map<String, Object>> getUserPermissions(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        List<UserPermissionResponse> permissions = userAccessManagementService.getUserPermissions(userId);
        return ApiResponse.success("User permissions retrieved successfully", permissions);
    }

    // ==================== USER ACCESS OVERVIEW ====================

    @GetMapping("/{userId}/access")
    @PreAuthorize("hasAuthority('manage_user_access') or hasAuthority('list_user_roles') or hasAuthority('list_user_permissions')")
    @Operation(summary = "Get user access overview", description = "Get complete access overview including roles, permissions, and effective permissions")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User access overview retrieved successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Map<String, Object>> getUserAccess(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        UserAccessResponse accessResponse = userAccessManagementService.getUserAccess(userId);
        return ApiResponse.success("User access overview retrieved successfully", accessResponse);
    }

    // ==================== BATCH OPERATIONS ====================

    @PostMapping("/{userId}/roles/batch")
    @PreAuthorize("hasAuthority('assign_user_role')")
    @Operation(summary = "Assign multiple roles to user", description = "Assign multiple roles to a specific user in batch")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Roles assigned successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or some roles not found")
    public ResponseEntity<Map<String, Object>> assignMultipleRolesToUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody List<UserRoleRequest> requests) {
        
        List<UserRoleResponse> responses = requests.stream()
                .map(request -> userAccessManagementService.assignRoleToUser(
                        userId, request.getRoleId(), request.getReason()))
                .toList();
        
        return ApiResponse.success("Roles assigned successfully", responses);
    }

    @PostMapping("/{userId}/permissions/batch")
    @PreAuthorize("hasAuthority('assign_user_permission')")
    @Operation(summary = "Assign multiple permissions to user", description = "Assign multiple direct permissions to a specific user in batch")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Permissions assigned successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or some permissions not found")
    public ResponseEntity<Map<String, Object>> assignMultiplePermissionsToUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody List<UserPermissionRequest> requests) {
        
        List<UserPermissionResponse> responses = requests.stream()
                .map(request -> userAccessManagementService.assignPermissionToUser(
                        userId, request.getPermissionId(), request.getReason()))
                .toList();
        
        return ApiResponse.success("Permissions assigned successfully", responses);
    }
}
