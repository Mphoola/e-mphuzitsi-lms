package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.role.PermissionResponse;
import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(permission -> PermissionResponse.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
