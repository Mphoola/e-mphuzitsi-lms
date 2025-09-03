package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RoleRepository Tests")
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    private Role testRole;
    private Role anotherRole;
    private Permission testPermission;
    private Permission anotherPermission;
    private User testUser;
    private User anotherUser;

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
                .permissions(Set.of(testPermission))
                .build();
        testRole = entityManager.persistAndFlush(testRole);

        anotherRole = Role.builder()
                .name("ANOTHER_ROLE")
                .permissions(Set.of(testPermission, anotherPermission))
                .build();
        anotherRole = entityManager.persistAndFlush(anotherRole);

        // Create test users
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        anotherUser = User.builder()
                .name("Another User")
                .email("another@example.com")
                .password("password456")
                .build();
        anotherUser = entityManager.persistAndFlush(anotherUser);

        // Create user-role relationships
        UserRole userRole1 = UserRole.builder()
                .user(testUser)
                .role(testRole)
                .build();
        entityManager.persistAndFlush(userRole1);

        UserRole userRole2 = UserRole.builder()
                .user(anotherUser)
                .role(testRole)
                .build();
        entityManager.persistAndFlush(userRole2);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find role by name")
    void should_find_role_by_name() {
        // When
        Optional<Role> foundRole = roleRepository.findByName("TEST_ROLE");

        // Then
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("TEST_ROLE");
        assertThat(foundRole.get().getId()).isEqualTo(testRole.getId());
    }

    @Test
    @DisplayName("Should return empty when role not found by name")
    void should_return_empty_when_role_not_found_by_name() {
        // When
        Optional<Role> foundRole = roleRepository.findByName("NON_EXISTENT_ROLE");

        // Then
        assertThat(foundRole).isEmpty();
    }

    @Test
    @DisplayName("Should check if role exists by name")
    void should_check_if_role_exists_by_name() {
        // When & Then
        assertThat(roleRepository.existsByName("TEST_ROLE")).isTrue();
        assertThat(roleRepository.existsByName("NON_EXISTENT_ROLE")).isFalse();
    }

    @Test
    @DisplayName("Should find role by name with permissions")
    void should_find_role_by_name_with_permissions() {
        // When
        Optional<Role> foundRole = roleRepository.findByNameWithPermissions("TEST_ROLE");

        // Then
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("TEST_ROLE");
        assertThat(foundRole.get().getPermissions()).hasSize(1);
        assertThat(foundRole.get().getPermissions())
                .extracting(Permission::getName)
                .containsExactly("test_permission");
    }

    @Test
    @DisplayName("Should find role by ID with permissions")
    void should_find_role_by_id_with_permissions() {
        // When
        Optional<Role> foundRole = roleRepository.findByIdWithPermissions(anotherRole.getId());

        // Then
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo("ANOTHER_ROLE");
        assertThat(foundRole.get().getPermissions()).hasSize(2);
        assertThat(foundRole.get().getPermissions())
                .extracting(Permission::getName)
                .containsExactlyInAnyOrder("test_permission", "another_permission");
    }

    @Test
    @DisplayName("Should count users by role ID")
    void should_count_users_by_role_id() {
        // When
        long userCount = roleRepository.countUsersByRoleId(testRole.getId());

        // Then
        assertThat(userCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return zero count when no users have the role")
    void should_return_zero_count_when_no_users_have_role() {
        // When
        long userCount = roleRepository.countUsersByRoleId(anotherRole.getId());

        // Then
        assertThat(userCount).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should find users by role ID")
    void should_find_users_by_role_id() {
        // When
        List<User> users = roleRepository.findUsersByRoleId(testRole.getId());

        // Then
        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("test@example.com", "another@example.com");
    }

    @Test
    @DisplayName("Should return empty list when no users have the role")
    void should_return_empty_list_when_no_users_have_role() {
        // When
        List<User> users = roleRepository.findUsersByRoleId(anotherRole.getId());

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Should check if role exists by name and ID not equal")
    void should_check_if_role_exists_by_name_and_id_not_equal() {
        // When & Then
        assertThat(roleRepository.existsByNameAndIdNot("TEST_ROLE", anotherRole.getId())).isTrue();
        assertThat(roleRepository.existsByNameAndIdNot("TEST_ROLE", testRole.getId())).isFalse();
        assertThat(roleRepository.existsByNameAndIdNot("NON_EXISTENT", testRole.getId())).isFalse();
    }

    @Test
    @DisplayName("Should save new role")
    void should_save_new_role() {
        // Given
        Role newRole = Role.builder()
                .name("NEW_ROLE")
                .permissions(new HashSet<>())
                .build();

        // When
        Role savedRole = roleRepository.save(newRole);

        // Then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull();
        assertThat(savedRole.getName()).isEqualTo("NEW_ROLE");
        assertThat(savedRole.getCreatedAt()).isNotNull();
        assertThat(savedRole.getUpdatedAt()).isNotNull();

        // Verify it was persisted
        Optional<Role> foundRole = roleRepository.findById(savedRole.getId());
        assertThat(foundRole).isPresent();
    }

    @Test
    @DisplayName("Should update existing role")
    void should_update_existing_role() {
        // Given
        Role roleToUpdate = roleRepository.findById(testRole.getId()).orElseThrow();
        String originalName = roleToUpdate.getName();
        
        // When
        roleToUpdate.setName("UPDATED_ROLE");
        roleToUpdate.getPermissions().add(anotherPermission);
        Role updatedRole = roleRepository.save(roleToUpdate);

        // Then
        assertThat(updatedRole.getName()).isEqualTo("UPDATED_ROLE");
        assertThat(updatedRole.getName()).isNotEqualTo(originalName);
        assertThat(updatedRole.getPermissions()).hasSize(2);
        assertThat(updatedRole.getUpdatedAt()).isNotNull();
        
        // Verify persistence
        Role foundRole = roleRepository.findById(testRole.getId()).orElseThrow();
        assertThat(foundRole.getName()).isEqualTo("UPDATED_ROLE");
    }

    @Test
    @DisplayName("Should delete role by ID")
    void should_delete_role_by_id() {
        // Given
        Long roleId = anotherRole.getId();
        assertThat(roleRepository.existsById(roleId)).isTrue();

        // When
        roleRepository.deleteById(roleId);
        entityManager.flush();

        // Then
        assertThat(roleRepository.existsById(roleId)).isFalse();
        Optional<Role> foundRole = roleRepository.findById(roleId);
        assertThat(foundRole).isEmpty();
    }

    @Test
    @DisplayName("Should find all roles")
    void should_find_all_roles() {
        // When
        List<Role> allRoles = roleRepository.findAll();

        // Then
        assertThat(allRoles).hasSize(2);
        assertThat(allRoles)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("TEST_ROLE", "ANOTHER_ROLE");
    }

    @Test
    @DisplayName("Should handle role with no permissions")
    void should_handle_role_with_no_permissions() {
        // Given
        Role roleWithoutPermissions = Role.builder()
                .name("EMPTY_ROLE")
                .permissions(new HashSet<>())
                .build();

        // When
        Role savedRole = roleRepository.save(roleWithoutPermissions);

        // Then
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getPermissions()).isEmpty();

        // Verify with permissions query
        Optional<Role> foundWithPermissions = roleRepository.findByIdWithPermissions(savedRole.getId());
        assertThat(foundWithPermissions).isPresent();
        assertThat(foundWithPermissions.get().getPermissions()).isEmpty();
    }
}
