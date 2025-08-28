package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;



    private User testUser;
    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        // Create test permission
        testPermission = Permission.builder()
                .name("test_permission")
                .build();
        testPermission = entityManager.persistAndFlush(testPermission);

        // Create test role with permission
        testRole = Role.builder()
                .name("TEST_ROLE")
                .permissions(Set.of(testPermission))
                .build();
        testRole = entityManager.persistAndFlush(testRole);

        // Create test user
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword123")
                .resetToken("reset123")
                .resetTokenExpiresAt(LocalDateTime.now().plusHours(1))
                .build();
        testUser = entityManager.persistAndFlush(testUser);

        // Create user-role relationship
        UserRole userRole = UserRole.builder()
                .user(testUser)
                .role(testRole)
                .build();
        entityManager.persistAndFlush(userRole);

        // Create user-permission relationship
        UserPermission userPermission = UserPermission.builder()
                .user(testUser)
                .permission(testPermission)
                .build();
        entityManager.persistAndFlush(userPermission);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find user by email")
    void should_find_user_by_email() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Test User");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void should_return_empty_when_user_not_found_by_email() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void should_check_if_user_exists_by_email() {
        // When & Then
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should find user by reset token")
    void should_find_user_by_reset_token() {
        // When
        Optional<User> foundUser = userRepository.findByResetToken("reset123");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getResetToken()).isEqualTo("reset123");
    }

    @Test
    @DisplayName("Should return empty when reset token not found")
    void should_return_empty_when_reset_token_not_found() {
        // When
        Optional<User> foundUser = userRepository.findByResetToken("nonexistent");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by email with roles and permissions")
    void should_find_user_by_email_with_roles_and_permissions() {
        // When
        Optional<User> foundUser = userRepository.findByEmailWithRolesAndPermissions("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        User user = foundUser.get();
        
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getName()).isEqualTo("Test User");
        
        // Check if roles are loaded
        assertThat(user.getUserRoles()).hasSize(1);
        UserRole userRole = user.getUserRoles().iterator().next();
        assertThat(userRole.getRole().getName()).isEqualTo("TEST_ROLE");
        
        // Check if permissions are loaded through role
        assertThat(userRole.getRole().getPermissions()).hasSize(1);
        Permission rolePermission = userRole.getRole().getPermissions().iterator().next();
        assertThat(rolePermission.getName()).isEqualTo("test_permission");
        
        // Check if direct user permissions are loaded
        assertThat(user.getUserPermissions()).hasSize(1);
        UserPermission userPermission = user.getUserPermissions().iterator().next();
        assertThat(userPermission.getPermission().getName()).isEqualTo("test_permission");
    }

    @Test
    @DisplayName("Should find user by ID with roles and permissions")
    void should_find_user_by_id_with_roles_and_permissions() {
        // When
        Optional<User> foundUser = userRepository.findByIdWithRolesAndPermissions(testUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        User user = foundUser.get();
        
        assertThat(user.getId()).isEqualTo(testUser.getId());
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        
        // Check if roles are loaded
        assertThat(user.getUserRoles()).hasSize(1);
        UserRole userRole = user.getUserRoles().iterator().next();
        assertThat(userRole.getRole().getName()).isEqualTo("TEST_ROLE");
        
        // Check if permissions are loaded
        assertThat(user.getUserPermissions()).hasSize(1);
        UserPermission userPermission = user.getUserPermissions().iterator().next();
        assertThat(userPermission.getPermission().getName()).isEqualTo("test_permission");
    }

    @Test
    @DisplayName("Should return empty when finding non-existent user by email with roles and permissions")
    void should_return_empty_when_finding_nonexistent_user_by_email_with_roles_and_permissions() {
        // When
        Optional<User> foundUser = userRepository.findByEmailWithRolesAndPermissions("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when finding non-existent user by ID with roles and permissions")
    void should_return_empty_when_finding_nonexistent_user_by_id_with_roles_and_permissions() {
        // When
        Optional<User> foundUser = userRepository.findByIdWithRolesAndPermissions(99999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should save and retrieve user")
    void should_save_and_retrieve_user() {
        // Given
        User newUser = User.builder()
                .name("New Test User")
                .email("new@example.com")
                .password("password123")
                .build();

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("New Test User");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        // Verify retrieval
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Should delete user")
    void should_delete_user() {
        // Given
        User userToDelete = User.builder()
                .name("User To Delete")
                .email("delete@example.com")
                .password("password123")
                .build();
        userToDelete = userRepository.save(userToDelete);

        // When
        userRepository.delete(userToDelete);

        // Then
        Optional<User> deletedUser = userRepository.findById(userToDelete.getId());
        assertThat(deletedUser).isEmpty();
    }
}
