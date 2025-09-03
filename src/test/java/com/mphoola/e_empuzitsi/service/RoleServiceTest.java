package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.RoleRequest;
import com.mphoola.e_empuzitsi.dto.RoleResponse;
import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.RoleInUseException;
import com.mphoola.e_empuzitsi.repository.PermissionRepository;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleService
 * Tests role CRUD operations, validation, and business logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private Permission testPermission;
    private User testUser;
    private RoleRequest roleRequest;

    @BeforeEach
    void setUp() {
        testPermission = Permission.builder()
                .id(1L)
                .name("test_permission")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testRole = Role.builder()
                .id(1L)
                .name("TEST_ROLE")
                .permissions(Set.of(testPermission))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        roleRequest = RoleRequest.builder()
                .name("NEW_ROLE")
                .permissionIds(Set.of(1L))
                .build();
    }

    @Test
    @DisplayName("Should create new role successfully")
    void should_create_new_role_successfully() {
        // Given
        when(roleRepository.existsByName(roleRequest.getName())).thenReturn(false);
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        RoleResponse result = roleService.createRole(roleRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("TEST_ROLE");
        assertThat(result.getUserCount()).isEqualTo(0L);
        assertThat(result.getPermissionCount()).isEqualTo(0L);

        verify(roleRepository).existsByName(roleRequest.getName());
        verify(permissionRepository).findById(1L);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw ResourceConflictException when creating role with existing name")
    void should_throw_exception_when_creating_role_with_existing_name() {
        // Given
        when(roleRepository.existsByName(roleRequest.getName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roleService.createRole(roleRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Role already exists with name: NEW_ROLE");

        verify(roleRepository).existsByName(roleRequest.getName());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating role with invalid permission ID")
    void should_throw_exception_when_creating_role_with_invalid_permission() {
        // Given
        when(roleRepository.existsByName(roleRequest.getName())).thenReturn(false);
        when(permissionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.createRole(roleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Permission not found with id: 1");

        verify(roleRepository).existsByName(roleRequest.getName());
        verify(permissionRepository).findById(1L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should update existing role successfully")
    void should_update_existing_role_successfully() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByNameAndIdNot(roleRequest.getName(), roleId)).thenReturn(false);
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        RoleResponse result = roleService.updateRole(roleId, roleRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("NEW_ROLE");

        verify(roleRepository).findByIdWithPermissions(roleId);
        verify(roleRepository).existsByNameAndIdNot(roleRequest.getName(), roleId);
        verify(permissionRepository).findById(1L);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent role")
    void should_throw_exception_when_updating_non_existent_role() {
        // Given
        Long roleId = 999L;
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.updateRole(roleId, roleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found with id: 999");

        verify(roleRepository).findByIdWithPermissions(roleId);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw ResourceConflictException when updating role with existing name")
    void should_throw_exception_when_updating_role_with_existing_name() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByNameAndIdNot(roleRequest.getName(), roleId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> roleService.updateRole(roleId, roleRequest))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Role already exists with name: NEW_ROLE");

        verify(roleRepository).findByIdWithPermissions(roleId);
        verify(roleRepository).existsByNameAndIdNot(roleRequest.getName(), roleId);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should delete role successfully")
    void should_delete_role_successfully() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.countUsersByRoleId(roleId)).thenReturn(0L);

        // When
        roleService.deleteRole(roleId);

        // Then
        verify(roleRepository).findById(roleId);
        verify(roleRepository).countUsersByRoleId(roleId);
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent role")
    void should_throw_exception_when_deleting_non_existent_role() {
        // Given
        Long roleId = 999L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found with id: 999");

        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw RoleInUseException when deleting role with users")
    void should_throw_exception_when_deleting_role_with_users() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.countUsersByRoleId(roleId)).thenReturn(2L);

        // When & Then
        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(RoleInUseException.class)
                .hasMessage("Cannot delete role 'TEST_ROLE' as it is assigned to 2 user(s)");

        verify(roleRepository).findById(roleId);
        verify(roleRepository).countUsersByRoleId(roleId);
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should get role by ID with details successfully")
    void should_get_role_by_id_with_details_successfully() {
        // Given
        Long roleId = 1L;
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.findUsersByRoleId(roleId)).thenReturn(List.of(testUser));

        // When
        RoleResponse result = roleService.getRoleById(roleId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("TEST_ROLE");
        assertThat(result.getPermissions()).hasSize(1);
        assertThat(result.getUsers()).hasSize(1);
        assertThat(result.getUserCount()).isEqualTo(1L);
        assertThat(result.getPermissionCount()).isEqualTo(1L);

        verify(roleRepository).findByIdWithPermissions(roleId);
        verify(roleRepository).findUsersByRoleId(roleId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting non-existent role by ID")
    void should_throw_exception_when_getting_non_existent_role_by_id() {
        // Given
        Long roleId = 999L;
        when(roleRepository.findByIdWithPermissions(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.getRoleById(roleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found with id: 999");

        verify(roleRepository).findByIdWithPermissions(roleId);
        verify(roleRepository, never()).findUsersByRoleId(any());
    }

    @Test
    @DisplayName("Should get all roles with counts successfully")
    void should_get_all_roles_with_counts_successfully() {
        // Given
        List<Role> roles = List.of(testRole);
        when(roleRepository.findAll()).thenReturn(roles);
        when(roleRepository.countUsersByRoleId(testRole.getId())).thenReturn(2L);

        // When
        List<RoleResponse> result = roleService.getAllRoles();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("TEST_ROLE");
        assertThat(result.get(0).getUserCount()).isEqualTo(2L);
        assertThat(result.get(0).getPermissionCount()).isEqualTo(1L);
        assertThat(result.get(0).getPermissions()).isEmpty(); // Empty for list view
        assertThat(result.get(0).getUsers()).isEmpty(); // Empty for list view

        verify(roleRepository).findAll();
        verify(roleRepository).countUsersByRoleId(testRole.getId());
    }

    @Test
    @DisplayName("Should get role by name successfully")
    void should_get_role_by_name_successfully() {
        // Given
        String roleName = "TEST_ROLE";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(testRole));

        // When
        RoleResponse result = roleService.getRoleByName(roleName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("TEST_ROLE");
        assertThat(result.getPermissions()).isEmpty(); // No permissions loaded
        assertThat(result.getUsers()).isEmpty(); // No users loaded

        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting non-existent role by name")
    void should_throw_exception_when_getting_non_existent_role_by_name() {
        // Given
        String roleName = "NON_EXISTENT_ROLE";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> roleService.getRoleByName(roleName))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found with name: NON_EXISTENT_ROLE");

        verify(roleRepository).findByName(roleName);
    }

    @Test
    @DisplayName("Should handle role with empty permissions")
    void should_handle_role_with_empty_permissions() {
        // Given
        Role roleWithoutPermissions = Role.builder()
                .id(2L)
                .name("EMPTY_ROLE")
                .permissions(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(roleRepository.findByIdWithPermissions(2L)).thenReturn(Optional.of(roleWithoutPermissions));
        when(roleRepository.findUsersByRoleId(2L)).thenReturn(Collections.emptyList());

        // When
        RoleResponse result = roleService.getRoleById(2L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPermissions()).isEmpty();
        assertThat(result.getUsers()).isEmpty();
        assertThat(result.getUserCount()).isEqualTo(0L);
        assertThat(result.getPermissionCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should create role with empty permissions")
    void should_create_role_with_empty_permissions() {
        // Given
        RoleRequest emptyPermissionsRequest = RoleRequest.builder()
                .name("EMPTY_ROLE")
                .permissionIds(new HashSet<>())
                .build();

        Role savedRole = Role.builder()
                .id(2L)
                .name("EMPTY_ROLE")
                .permissions(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(roleRepository.existsByName(emptyPermissionsRequest.getName())).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        // When
        RoleResponse result = roleService.createRole(emptyPermissionsRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("EMPTY_ROLE");
        assertThat(result.getPermissionCount()).isEqualTo(0L);

        verify(roleRepository).existsByName(emptyPermissionsRequest.getName());
        verify(roleRepository).save(any(Role.class));
        verifyNoInteractions(permissionRepository);
    }
}
