package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.PermissionResponse;
import com.mphoola.e_empuzitsi.dto.RoleRequest;
import com.mphoola.e_empuzitsi.dto.RoleResponse;
import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.RoleInUseException;
import com.mphoola.e_empuzitsi.repository.PermissionRepository;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }
    
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating new role: {}", request.getName());
        
        if (roleRepository.existsByName(request.getName())) {
            throw new ResourceConflictException("Role already exists with name: " + request.getName());
        }
        
        Set<Permission> permissions = validateAndFetchPermissions(request.getPermissionIds());
        
        Role role = Role.builder()
                .name(request.getName())
                .permissions(permissions)
                .build();
        
        Role savedRole = roleRepository.save(role);
        log.info("Successfully created role: {} with ID: {}", savedRole.getName(), savedRole.getId());
        
        return mapToRoleResponse(savedRole);
    }
    
    public RoleResponse updateRole(Long id, RoleRequest request) {
        log.info("Updating role with ID: {}", id);
        
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
        log.info("Successfully updated role: {} with ID: {}", updatedRole.getName(), updatedRole.getId());
        
        return mapToRoleResponse(updatedRole);
    }
    
    public void deleteRole(Long id) {
        log.info("Deleting role with ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        long userCount = roleRepository.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new RoleInUseException("Cannot delete role '" + role.getName() + 
                    "' as it is assigned to " + userCount + " user(s)");
        }
        
        roleRepository.deleteById(id);
        log.info("Successfully deleted role: {} with ID: {}", role.getName(), role.getId());
    }
    
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        log.debug("Fetching role with ID: {}", id);
        
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        return mapToRoleResponse(role);
    }
    
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        log.debug("Fetching role with name: {}", name);
        
        Role role = roleRepository.findByNameWithPermissions(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        
        return mapToRoleResponse(role);
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
}
