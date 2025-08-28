package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.MessageResponse;
import com.mphoola.e_empuzitsi.dto.RoleRequest;
import com.mphoola.e_empuzitsi.dto.RoleResponse;
import com.mphoola.e_empuzitsi.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_role')")
    @Operation(summary = "Update an existing role")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_role')")
    @Operation(summary = "Delete a role")
    public ResponseEntity<MessageResponse> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        MessageResponse response = MessageResponse.builder()
                .message("Role deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('view_role_details')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('list_roles')")
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(response);
    }
}
