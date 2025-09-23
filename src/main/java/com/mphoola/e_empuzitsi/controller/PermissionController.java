package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.role.PermissionResponse;
import com.mphoola.e_empuzitsi.service.PermissionService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@PreAuthorize("hasAuthority('list_permissions')")
@Tag(name = "Permission Management", description = "APIs for managing permissions")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('list_permissions')")
    @Operation(summary = "List all available permissions")
    public ResponseEntity<?> listPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ApiResponse.success("Permissions retrieved successfully", permissions);
    }
}
