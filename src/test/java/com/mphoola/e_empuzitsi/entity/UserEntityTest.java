package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA tests for User entity - similar to Laravel's feature tests
 * Tests entity creation, relationships, and timestamp functionality
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void should_create_user_with_timestamps() {
        // Given - like Laravel's factory creation
        User user = User.builder()
            .name("John Doe")
            .email("john@example.com")
            .password("password123")
            .build();

        // When - save the entity
        User savedUser = entityManager.persistAndFlush(user);

        // Then - verify entity creation and timestamps
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
        
        // Verify timestamps are automatically set (Laravel-style)
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(savedUser.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_user_and_modify_updated_at_timestamp() {
        // Given - create and save a user
        User user = User.builder()
            .name("Jane Doe")
            .email("jane@example.com")
            .password("password123")
            .build();
        User savedUser = entityManager.persistAndFlush(user);
        LocalDateTime originalUpdatedAt = savedUser.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - update the user
        savedUser.setName("Jane Smith");
        User updatedUser = entityManager.persistAndFlush(savedUser);

        // Then - verify updated_at timestamp changed
        assertThat(updatedUser.getName()).isEqualTo("Jane Smith");
        assertThat(updatedUser.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedUser.getCreatedAt()).isEqualTo(savedUser.getCreatedAt()); // created_at should not change
    }

    @Test
    public void should_create_user_with_roles_relationship() {
        // Given - create role and user
        Role role = Role.builder()
            .name("STUDENT")
            .build();
        Role savedRole = entityManager.persistAndFlush(role);

        User user = User.builder()
            .name("Student User")
            .email("student@example.com")
            .password("password123")
            .build();
        User savedUser = entityManager.persistAndFlush(user);

        // When - create user-role relationship
        UserRole userRole = UserRole.builder()
            .user(savedUser)
            .role(savedRole)
            .build();
        UserRole savedUserRole = entityManager.persistAndFlush(userRole);

        // Then - verify relationship and timestamps
        assertThat(savedUserRole.getId()).isNotNull();
        assertThat(savedUserRole.getUser()).isEqualTo(savedUser);
        assertThat(savedUserRole.getRole()).isEqualTo(savedRole);
        assertThat(savedUserRole.getCreatedAt()).isNotNull();
        assertThat(savedUserRole.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_create_user_with_permissions_relationship() {
        // Given - create permission and user
        Permission permission = Permission.builder()
            .name("upload_lesson")
            .build();
        Permission savedPermission = entityManager.persistAndFlush(permission);

        User user = User.builder()
            .name("Teacher User")
            .email("teacher@example.com")
            .password("password123")
            .build();
        User savedUser = entityManager.persistAndFlush(user);

        // When - create user-permission relationship
        UserPermission userPermission = UserPermission.builder()
            .user(savedUser)
            .permission(savedPermission)
            .build();
        UserPermission savedUserPermission = entityManager.persistAndFlush(userPermission);

        // Then - verify relationship and timestamps
        assertThat(savedUserPermission.getId()).isNotNull();
        assertThat(savedUserPermission.getUser()).isEqualTo(savedUser);
        assertThat(savedUserPermission.getPermission()).isEqualTo(savedPermission);
        assertThat(savedUserPermission.getCreatedAt()).isNotNull();
        assertThat(savedUserPermission.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_enforce_unique_email_constraint() {
        // Given - create first user
        User user1 = User.builder()
            .name("User One")
            .email("duplicate@example.com")
            .password("password123")
            .build();
        entityManager.persistAndFlush(user1);

        // When - try to create second user with same email
        User user2 = User.builder()
            .name("User Two")
            .email("duplicate@example.com")
            .password("password456")
            .build();

        // Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user2);
        }).isInstanceOf(ConstraintViolationException.class);
    }
}
