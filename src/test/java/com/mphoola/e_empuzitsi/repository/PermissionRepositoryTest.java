package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PermissionRepository Tests")
class PermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PermissionRepository permissionRepository;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        // Create test permission
        testPermission = Permission.builder()
                .name("test_permission")
                .build();
        testPermission = entityManager.persistAndFlush(testPermission);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find permission by name")
    void should_find_permission_by_name() {
        // When
        Optional<Permission> foundPermission = permissionRepository.findByName("test_permission");

        // Then
        assertThat(foundPermission).isPresent();
        assertThat(foundPermission.get().getName()).isEqualTo("test_permission");
        assertThat(foundPermission.get().getId()).isEqualTo(testPermission.getId());
    }

    @Test
    @DisplayName("Should return empty when permission not found by name")
    void should_return_empty_when_permission_not_found_by_name() {
        // When
        Optional<Permission> foundPermission = permissionRepository.findByName("nonexistent_permission");

        // Then
        assertThat(foundPermission).isEmpty();
    }

    @Test
    @DisplayName("Should check if permission exists by name")
    void should_check_if_permission_exists_by_name() {
        // When & Then
        assertThat(permissionRepository.existsByName("test_permission")).isTrue();
        assertThat(permissionRepository.existsByName("nonexistent_permission")).isFalse();
    }

    @Test
    @DisplayName("Should save and retrieve permission")
    void should_save_and_retrieve_permission() {
        // Given
        Permission newPermission = Permission.builder()
                .name("new_permission")
                .build();

        // When
        Permission savedPermission = permissionRepository.save(newPermission);

        // Then
        assertThat(savedPermission.getId()).isNotNull();
        assertThat(savedPermission.getName()).isEqualTo("new_permission");
        assertThat(savedPermission.getCreatedAt()).isNotNull();
        assertThat(savedPermission.getUpdatedAt()).isNotNull();

        // Verify retrieval
        Optional<Permission> retrievedPermission = permissionRepository.findById(savedPermission.getId());
        assertThat(retrievedPermission).isPresent();
        assertThat(retrievedPermission.get().getName()).isEqualTo("new_permission");
    }

    @Test
    @DisplayName("Should update permission")
    void should_update_permission() {
        // Given
        Permission permissionToUpdate = permissionRepository.findByName("test_permission").orElseThrow();
        String originalName = permissionToUpdate.getName();
        
        // When
        permissionToUpdate.setName("updated_permission");
        Permission updatedPermission = permissionRepository.save(permissionToUpdate);

        // Then
        assertThat(updatedPermission.getName()).isEqualTo("updated_permission");
        assertThat(updatedPermission.getId()).isEqualTo(permissionToUpdate.getId());
        
        // Verify the old name is no longer found
        Optional<Permission> oldPermission = permissionRepository.findByName(originalName);
        assertThat(oldPermission).isEmpty();
        
        // Verify new name is found
        Optional<Permission> newPermission = permissionRepository.findByName("updated_permission");
        assertThat(newPermission).isPresent();
    }

    @Test
    @DisplayName("Should delete permission")
    void should_delete_permission() {
        // Given
        Permission permissionToDelete = Permission.builder()
                .name("permission_to_delete")
                .build();
        permissionToDelete = permissionRepository.save(permissionToDelete);

        // When
        permissionRepository.delete(permissionToDelete);

        // Then
        Optional<Permission> deletedPermission = permissionRepository.findById(permissionToDelete.getId());
        assertThat(deletedPermission).isEmpty();
        
        // Verify by name as well
        Optional<Permission> deletedPermissionByName = permissionRepository.findByName("permission_to_delete");
        assertThat(deletedPermissionByName).isEmpty();
    }

    @Test
    @DisplayName("Should check existence of permission")
    void should_check_existence_of_permission() {
        // When & Then
        assertThat(permissionRepository.existsById(testPermission.getId())).isTrue();
        assertThat(permissionRepository.existsById(99999L)).isFalse();
    }

    @Test
    @DisplayName("Should count permissions")
    void should_count_permissions() {
        // Given - we have at least one permission from setup
        long initialCount = permissionRepository.count();
        
        // When - add another permission
        Permission additionalPermission = Permission.builder()
                .name("additional_permission")
                .build();
        permissionRepository.save(additionalPermission);

        // Then
        assertThat(permissionRepository.count()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("Should handle duplicate names gracefully")
    void should_handle_duplicate_names_gracefully() {
        // Given
        Permission duplicatePermission = Permission.builder()
                .name("test_permission")
                .build();

        // When & Then - This should throw an exception due to unique constraint
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> permissionRepository.saveAndFlush(duplicatePermission)
        );
    }

    @Test
    @DisplayName("Should find all permissions")
    void should_find_all_permissions() {
        // Given - add additional permissions
        Permission permission1 = Permission.builder().name("permission_1").build();
        Permission permission2 = Permission.builder().name("permission_2").build();
        
        permissionRepository.save(permission1);
        permissionRepository.save(permission2);

        // When
        java.util.List<Permission> allPermissions = permissionRepository.findAll();

        // Then - should have at least 3 (testPermission + 2 additional)
        assertThat(allPermissions).hasSizeGreaterThanOrEqualTo(3);
        assertThat(allPermissions.stream().map(Permission::getName))
                .contains("test_permission", "permission_1", "permission_2");
    }
}
