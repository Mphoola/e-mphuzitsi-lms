package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.entity.UserPermission;
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
@DisplayName("UserPermissionRepository Tests")
class UserPermissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserPermissionRepository userPermissionRepository;

    private User testUser;
    private User anotherUser;
    private Permission testPermission;
    private Permission anotherPermission;
    private UserPermission testUserPermission;

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

        // Create test permissions
        testPermission = Permission.builder()
                .name("test_permission")
                .build();
        testPermission = entityManager.persistAndFlush(testPermission);

        anotherPermission = Permission.builder()
                .name("another_permission")
                .build();
        anotherPermission = entityManager.persistAndFlush(anotherPermission);

        // Create test user-permission relationship
        testUserPermission = UserPermission.builder()
                .user(testUser)
                .permission(testPermission)
                .build();
        testUserPermission = entityManager.persistAndFlush(testUserPermission);

        // Create another user-permission relationship
        UserPermission anotherUserPermission = UserPermission.builder()
                .user(anotherUser)
                .permission(testPermission)
                .build();
        entityManager.persistAndFlush(anotherUserPermission);

        // Create another relationship for first user
        UserPermission userAnotherPermission = UserPermission.builder()
                .user(testUser)
                .permission(anotherPermission)
                .build();
        entityManager.persistAndFlush(userAnotherPermission);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find user permissions by user ID")
    void should_find_user_permissions_by_user_id() {
        // When
        List<UserPermission> userPermissions = userPermissionRepository.findByUserId(testUser.getId());

        // Then
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions.stream().map(up -> up.getPermission().getName()))
                .contains("test_permission", "another_permission");
    }

    @Test
    @DisplayName("Should find user permissions by permission ID")
    void should_find_user_permissions_by_permission_id() {
        // When
        List<UserPermission> userPermissions = userPermissionRepository.findByPermissionId(testPermission.getId());

        // Then
        assertThat(userPermissions).hasSize(2);
        assertThat(userPermissions.stream().map(up -> up.getUser().getEmail()))
                .contains("test@example.com", "another@example.com");
    }

    @Test
    @DisplayName("Should find user permission by user ID and permission ID")
    void should_find_user_permission_by_user_id_and_permission_id() {
        // When
        Optional<UserPermission> userPermission = userPermissionRepository.findByUserIdAndPermissionId(
                testUser.getId(), testPermission.getId());

        // Then
        assertThat(userPermission).isPresent();
        assertThat(userPermission.get().getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(userPermission.get().getPermission().getName()).isEqualTo("test_permission");
    }

    @Test
    @DisplayName("Should return empty when user permission not found by user ID and permission ID")
    void should_return_empty_when_user_permission_not_found() {
        // When
        Optional<UserPermission> userPermission = userPermissionRepository.findByUserIdAndPermissionId(99999L, 99999L);

        // Then
        assertThat(userPermission).isEmpty();
    }

    @Test
    @DisplayName("Should check if user permission exists")
    void should_check_if_user_permission_exists() {
        // When & Then
        assertThat(userPermissionRepository.existsByUserIdAndPermissionId(testUser.getId(), testPermission.getId()))
                .isTrue();
        assertThat(userPermissionRepository.existsByUserIdAndPermissionId(99999L, 99999L))
                .isFalse();
    }

    @Test
    @DisplayName("Should find user permissions by user ID with permission")
    void should_find_user_permissions_by_user_id_with_permission() {
        // When
        List<UserPermission> userPermissions = userPermissionRepository.findByUserIdWithPermission(testUser.getId());

        // Then
        assertThat(userPermissions).hasSize(2);
        
        // Verify permissions are eagerly loaded
        for (UserPermission userPermission : userPermissions) {
            assertThat(userPermission.getPermission()).isNotNull();
            assertThat(userPermission.getPermission().getName()).isIn("test_permission", "another_permission");
        }
    }

    @Test
    @DisplayName("Should return empty list when finding user permissions for non-existent user")
    void should_return_empty_list_when_finding_user_permissions_for_nonexistent_user() {
        // When
        List<UserPermission> userPermissions = userPermissionRepository.findByUserIdWithPermission(99999L);

        // Then
        assertThat(userPermissions).isEmpty();
    }

    @Test
    @DisplayName("Should save user permission relationship")
    void should_save_user_permission_relationship() {
        // Given
        User newUser = User.builder()
                .name("New User")
                .email("new@example.com")
                .password("password123")
                .build();
        newUser = entityManager.persistAndFlush(newUser);

        UserPermission newUserPermission = UserPermission.builder()
                .user(newUser)
                .permission(testPermission)
                .build();

        // When
        UserPermission savedUserPermission = userPermissionRepository.save(newUserPermission);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedUserPermission).isNotNull();
        assertThat(savedUserPermission.getUser().getId()).isEqualTo(newUser.getId());
        assertThat(savedUserPermission.getPermission().getId()).isEqualTo(testPermission.getId());
        assertThat(savedUserPermission.getCreatedAt()).isNotNull();
        assertThat(savedUserPermission.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should count user permissions")
    void should_count_user_permissions() {
        // Given - we have 3 user-permission relationships from setup
        long count = userPermissionRepository.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @Transactional
    @DisplayName("Should delete user permission by user ID and permission ID")
    void should_delete_user_permission_by_user_id_and_permission_id() {
        // Given
        long initialCount = userPermissionRepository.count();
        assertThat(userPermissionRepository.existsByUserIdAndPermissionId(testUser.getId(), testPermission.getId()))
                .isTrue();

        // When
        userPermissionRepository.deleteByUserIdAndPermissionId(testUser.getId(), testPermission.getId());
        entityManager.flush();

        // Then
        assertThat(userPermissionRepository.existsByUserIdAndPermissionId(testUser.getId(), testPermission.getId()))
                .isFalse();
        assertThat(userPermissionRepository.count()).isEqualTo(initialCount - 1);
    }

    @Test
    @DisplayName("Should find all user permissions")
    void should_find_all_user_permissions() {
        // When
        List<UserPermission> allUserPermissions = userPermissionRepository.findAll();

        // Then - should have 3 from setup
        assertThat(allUserPermissions).hasSize(3);
    }

    @Test
    @DisplayName("Should handle non-existent relationships gracefully")
    void should_handle_nonexistent_relationships_gracefully() {
        // When
        List<UserPermission> emptyResult1 = userPermissionRepository.findByUserId(99999L);
        List<UserPermission> emptyResult2 = userPermissionRepository.findByPermissionId(99999L);

        // Then
        assertThat(emptyResult1).isEmpty();
        assertThat(emptyResult2).isEmpty();
    }

    @Test
    @DisplayName("Should preserve audit timestamps")
    void should_preserve_audit_timestamps() {
        // Given
        UserPermission userPermission = userPermissionRepository.findByUserIdAndPermissionId(
                testUser.getId(), testPermission.getId()).orElseThrow();
        
        java.time.LocalDateTime originalCreatedAt = userPermission.getCreatedAt();
        java.time.LocalDateTime originalUpdatedAt = userPermission.getUpdatedAt();

        // When - update the relationship (no actual fields to change in this junction table)
        // We'll just re-save it
        entityManager.detach(userPermission);
        UserPermission updatedUserPermission = userPermissionRepository.save(userPermission);

        // Then - created timestamp should remain the same, updated should be newer or same
        assertThat(updatedUserPermission.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(updatedUserPermission.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}
