package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.role.RoleRequest;
import com.mphoola.e_empuzitsi.dto.role.RoleResponse;
import com.mphoola.e_empuzitsi.dto.role.RoleResponseSimple;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.dto.user.UserResponseSimple;
import com.mphoola.e_empuzitsi.service.RoleService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Role Management", description = "APIs for managing roles and their permissions")
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('add_role')")
    @Operation(summary = "Create a new role")
    public ResponseEntity<Map<String, Object>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ApiResponse.created(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_role')")
    @Operation(summary = "Update an existing role")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ApiResponse.success("Role updated successfully", response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_role')")
    @Operation(summary = "Delete a role")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success("Role deleted successfully");
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('show_role_details')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ApiResponse.success("Role retrieved successfully", response);
    }

    @GetMapping("/{roleId}/users")
    @PreAuthorize("hasAuthority('list_users_by_role')")
    @Operation(summary = "List users with a particular role")
    public ResponseEntity<Map<String, Object>> getUsersByRole(
            @PathVariable Long roleId,
            Pageable pageable // Spring injects page, size, sort from query params
    ) {
        Page<UserResponseSimple> users = roleService.getUsersByRole(roleId, pageable);
        return ApiResponse.success("Users retrieved successfully", users);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('list_roles')")
    @Operation(summary = "Get all roles")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        List<RoleResponseSimple> response = roleService.getAllRoles();
        return ApiResponse.success("Roles retrieved successfully", response);
    }
}
