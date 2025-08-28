package com.mphoola.e_empuzitsi.repository;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRoleRepository Tests")
class UserRoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User testUser;
    private User anotherUser;
    private Role testRole;
    private Role anotherRole;
    private UserRole testUserRole;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword123")
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        anotherUser = User.builder()
                .name("Another User")
                .email("another@example.com")
                .password("hashedPassword456")
                .build();
        anotherUser = entityManager.persistAndFlush(anotherUser);

        // Create test roles
        testRole = Role.builder()
                .name("TEST_ROLE")
                .build();
        testRole = entityManager.persistAndFlush(testRole);

        anotherRole = Role.builder()
                .name("ANOTHER_ROLE")
                .build();
        anotherRole = entityManager.persistAndFlush(anotherRole);

        // Create test user-role relationship
        testUserRole = UserRole.builder()
                .user(testUser)
                .role(testRole)
                .build();
        testUserRole = entityManager.persistAndFlush(testUserRole);

        // Create another user-role relationship
        UserRole anotherUserRole = UserRole.builder()
                .user(anotherUser)
                .role(testRole)
                .build();
        entityManager.persistAndFlush(anotherUserRole);

        // Create another relationship for first user
        UserRole userAnotherRole = UserRole.builder()
                .user(testUser)
                .role(anotherRole)
                .build();
        entityManager.persistAndFlush(userAnotherRole);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find user roles by user ID")
    void should_find_user_roles_by_user_id() {
        // When
        List<UserRole> userRoles = userRoleRepository.findByUserId(testUser.getId());

        // Then
        assertThat(userRoles).hasSize(2);
        assertThat(userRoles.stream().map(ur -> ur.getRole().getName()))
                .contains("TEST_ROLE", "ANOTHER_ROLE");
    }

    @Test
    @DisplayName("Should find user roles by role ID")
    void should_find_user_roles_by_role_id() {
        // When
        List<UserRole> userRoles = userRoleRepository.findByRoleId(testRole.getId());

        // Then
        assertThat(userRoles).hasSize(2);
        assertThat(userRoles.stream().map(ur -> ur.getUser().getEmail()))
                .contains("test@example.com", "another@example.com");
    }

    @Test
    @DisplayName("Should find user role by user ID and role ID")
    void should_find_user_role_by_user_id_and_role_id() {
        // When
        Optional<UserRole> userRole = userRoleRepository.findByUserIdAndRoleId(
                testUser.getId(), testRole.getId());

        // Then
        assertThat(userRole).isPresent();
        assertThat(userRole.get().getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(userRole.get().getRole().getName()).isEqualTo("TEST_ROLE");
    }

    @Test
    @DisplayName("Should return empty when user role not found by user ID and role ID")
    void should_return_empty_when_user_role_not_found() {
        // When
        Optional<UserRole> userRole = userRoleRepository.findByUserIdAndRoleId(99999L, 99999L);

        // Then
        assertThat(userRole).isEmpty();
    }

    @Test
    @DisplayName("Should check if user role exists")
    void should_check_if_user_role_exists() {
        // When & Then
        assertThat(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), testRole.getId()))
                .isTrue();
        assertThat(userRoleRepository.existsByUserIdAndRoleId(99999L, 99999L))
                .isFalse();
    }

    @Test
    @DisplayName("Should find user roles by user ID with role")
    void should_find_user_roles_by_user_id_with_role() {
        // When
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(testUser.getId());

        // Then
        assertThat(userRoles).hasSize(2);
        
        // Verify roles are eagerly loaded
        for (UserRole userRole : userRoles) {
            assertThat(userRole.getRole()).isNotNull();
            assertThat(userRole.getRole().getName()).isIn("TEST_ROLE", "ANOTHER_ROLE");
        }
    }

    @Test
    @DisplayName("Should return empty list when finding user roles for non-existent user")
    void should_return_empty_list_when_finding_user_roles_for_nonexistent_user() {
        // When
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(99999L);

        // Then
        assertThat(userRoles).isEmpty();
    }

    @Test
    @DisplayName("Should save user role relationship")
    void should_save_user_role_relationship() {
        // Given
        User newUser = User.builder()
                .name("New User")
                .email("new@example.com")
                .password("password123")
                .build();
        newUser = entityManager.persistAndFlush(newUser);

        UserRole newUserRole = UserRole.builder()
                .user(newUser)
                .role(testRole)
                .build();

        // When
        UserRole savedUserRole = userRoleRepository.save(newUserRole);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedUserRole).isNotNull();
        assertThat(savedUserRole.getUser().getId()).isEqualTo(newUser.getId());
        assertThat(savedUserRole.getRole().getId()).isEqualTo(testRole.getId());
        assertThat(savedUserRole.getCreatedAt()).isNotNull();
        assertThat(savedUserRole.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should count user roles")
    void should_count_user_roles() {
        // Given - we have 3 user-role relationships from setup
        long count = userRoleRepository.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @Transactional
    @DisplayName("Should delete user role by user ID and role ID")
    void should_delete_user_role_by_user_id_and_role_id() {
        // Given
        long initialCount = userRoleRepository.count();
        assertThat(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), testRole.getId()))
                .isTrue();

        // When
        userRoleRepository.deleteByUserIdAndRoleId(testUser.getId(), testRole.getId());
        entityManager.flush();

        // Then
        assertThat(userRoleRepository.existsByUserIdAndRoleId(testUser.getId(), testRole.getId()))
                .isFalse();
        assertThat(userRoleRepository.count()).isEqualTo(initialCount - 1);
    }

    @Test
    @DisplayName("Should find all user roles")
    void should_find_all_user_roles() {
        // When
        List<UserRole> allUserRoles = userRoleRepository.findAll();

        // Then - should have 3 from setup
        assertThat(allUserRoles).hasSize(3);
    }

    @Test
    @DisplayName("Should handle non-existent relationships gracefully")
    void should_handle_nonexistent_relationships_gracefully() {
        // When
        List<UserRole> emptyResult1 = userRoleRepository.findByUserId(99999L);
        List<UserRole> emptyResult2 = userRoleRepository.findByRoleId(99999L);

        // Then
        assertThat(emptyResult1).isEmpty();
        assertThat(emptyResult2).isEmpty();
    }
}
