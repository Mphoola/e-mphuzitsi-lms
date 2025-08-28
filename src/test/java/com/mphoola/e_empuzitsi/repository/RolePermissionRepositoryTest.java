package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.RolePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RolePermissionRepository Tests")
class RolePermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    private Role testRole;
    private Role anotherRole;
    private Permission testPermission;
    private Permission anotherPermission;
    private RolePermission testRolePermission;

    @BeforeEach
    void setUp() {
        // Create test permissions
        testPermission = Permission.builder()
                .name("test_permission")
                .build();
        testPermission = entityManager.persistAndFlush(testPermission);

        anotherPermission = Permission.builder()
                .name("another_permission")
                .build();
        anotherPermission = entityManager.persistAndFlush(anotherPermission);

        // Create test roles
        testRole = Role.builder()
                .name("TEST_ROLE")
                .build();
        testRole = entityManager.persistAndFlush(testRole);

        anotherRole = Role.builder()
                .name("ANOTHER_ROLE")
                .build();
        anotherRole = entityManager.persistAndFlush(anotherRole);

        // Create role-permission relationships
        testRolePermission = RolePermission.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
        testRolePermission = entityManager.persistAndFlush(testRolePermission);

        RolePermission anotherRolePermission = RolePermission.builder()
                .role(testRole)
                .permission(anotherPermission)
                .build();
        entityManager.persistAndFlush(anotherRolePermission);

        RolePermission roleAnotherPermission = RolePermission.builder()
                .role(anotherRole)
                .permission(testPermission)
                .build();
        entityManager.persistAndFlush(roleAnotherPermission);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find permission names by role ID")
    void should_find_permission_names_by_role_id() {
        // When
        List<String> permissionNames = rolePermissionRepository.findPermissionNamesByRoleId(testRole.getId());

        // Then
        assertThat(permissionNames).hasSize(2);
        assertThat(permissionNames).contains("test_permission", "another_permission");
    }

    @Test
    @DisplayName("Should return empty list when finding permission names for non-existent role")
    void should_return_empty_list_when_finding_permission_names_for_nonexistent_role() {
        // When
        List<String> permissionNames = rolePermissionRepository.findPermissionNamesByRoleId(99999L);

        // Then
        assertThat(permissionNames).isEmpty();
    }

    @Test
    @DisplayName("Should find role permissions by role ID")
    void should_find_role_permissions_by_role_id() {
        // When
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(testRole.getId());

        // Then
        assertThat(rolePermissions).hasSize(2);
        assertThat(rolePermissions.stream().map(rp -> rp.getPermission().getName()))
                .contains("test_permission", "another_permission");
    }

    @Test
    @DisplayName("Should find role permissions by permission ID")
    void should_find_role_permissions_by_permission_id() {
        // When
        List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(testPermission.getId());

        // Then
        assertThat(rolePermissions).hasSize(2);
        assertThat(rolePermissions.stream().map(rp -> rp.getRole().getName()))
                .contains("TEST_ROLE", "ANOTHER_ROLE");
    }

    @Test
    @DisplayName("Should return empty list when finding role permissions for non-existent role")
    void should_return_empty_list_when_finding_role_permissions_for_nonexistent_role() {
        // When
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(99999L);

        // Then
        assertThat(rolePermissions).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when finding role permissions for non-existent permission")
    void should_return_empty_list_when_finding_role_permissions_for_nonexistent_permission() {
        // When
        List<RolePermission> rolePermissions = rolePermissionRepository.findByPermissionId(99999L);

        // Then
        assertThat(rolePermissions).isEmpty();
    }

    @Test
    @DisplayName("Should save role permission relationship")
    void should_save_role_permission_relationship() {
        // Given
        Permission newPermission = Permission.builder()
                .name("new_permission")
                .build();
        newPermission = entityManager.persistAndFlush(newPermission);

        RolePermission newRolePermission = RolePermission.builder()
                .role(testRole)
                .permission(newPermission)
                .build();

        // When
        RolePermission savedRolePermission = rolePermissionRepository.save(newRolePermission);

        // Then
        assertThat(savedRolePermission.getRole().getId()).isEqualTo(testRole.getId());
        assertThat(savedRolePermission.getPermission().getId()).isEqualTo(newPermission.getId());
    }

    @Test
    @DisplayName("Should count role permissions")
    void should_count_role_permissions() {
        // Given - we have 3 role-permission relationships from setup
        long count = rolePermissionRepository.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should find all role permissions")
    void should_find_all_role_permissions() {
        // When
        List<RolePermission> allRolePermissions = rolePermissionRepository.findAll();

        // Then - should have 3 from setup
        assertThat(allRolePermissions).hasSize(3);
    }

    @Test
    @DisplayName("Should delete role permission")
    void should_delete_role_permission() {
        // Given
        long initialCount = rolePermissionRepository.count();
        RolePermission.RolePermissionId id = new RolePermission.RolePermissionId(
                testRole.getId(), testPermission.getId());

        // When
        rolePermissionRepository.deleteById(id);

        // Then
        assertThat(rolePermissionRepository.count()).isEqualTo(initialCount - 1);
        
        // Verify the specific relationship is gone
        List<RolePermission> remainingForRole = rolePermissionRepository.findByRoleId(testRole.getId());
        assertThat(remainingForRole).hasSize(1);
        assertThat(remainingForRole.get(0).getPermission().getName()).isEqualTo("another_permission");
    }

    @Test
    @DisplayName("Should verify relationships are properly loaded")
    void should_verify_relationships_are_properly_loaded() {
        // When
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(testRole.getId());

        // Then
        for (RolePermission rolePermission : rolePermissions) {
            // Verify role and permission entities are loaded
            assertThat(rolePermission.getRole()).isNotNull();
            assertThat(rolePermission.getRole().getName()).isEqualTo("TEST_ROLE");
            assertThat(rolePermission.getPermission()).isNotNull();
            assertThat(rolePermission.getPermission().getName()).isIn("test_permission", "another_permission");
        }
    }

    @Test
    @DisplayName("Should find role permissions with specific permission name")
    void should_find_role_permissions_with_specific_permission_name() {
        // When
        List<String> permissionNames = rolePermissionRepository.findPermissionNamesByRoleId(testRole.getId());

        // Then
        assertThat(permissionNames).contains("test_permission");
        assertThat(permissionNames).contains("another_permission");
    }

    @Test
    @DisplayName("Should handle composite key operations")
    void should_handle_composite_key_operations() {
        // Given
        RolePermission.RolePermissionId compositeKey = new RolePermission.RolePermissionId(
                testRole.getId(), testPermission.getId());

        // When
        boolean exists = rolePermissionRepository.existsById(compositeKey);

        // Then
        assertThat(exists).isTrue();

        // When finding by composite key
        java.util.Optional<RolePermission> found = rolePermissionRepository.findById(compositeKey);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRole().getName()).isEqualTo("TEST_ROLE");
        assertThat(found.get().getPermission().getName()).isEqualTo("test_permission");
    }

    @Test
    @DisplayName("Should verify composite key consistency")
    void should_verify_composite_key_consistency() {
        // Given
        RolePermission rolePermission = rolePermissionRepository.findByRoleId(testRole.getId()).get(0);
        
        // When - verify the relationship exists
        RolePermission.RolePermissionId compositeKey = new RolePermission.RolePermissionId(
                rolePermission.getRole().getId(), 
                rolePermission.getPermission().getId());
        
        // Then
        assertThat(rolePermissionRepository.existsById(compositeKey)).isTrue();
    }
}
