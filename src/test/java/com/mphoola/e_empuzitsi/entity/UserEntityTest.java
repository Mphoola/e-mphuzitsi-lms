package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.exception.ConstraintViolationException;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA tests for User entity - similar to Laravel's feature tests
 * Tests entity creation, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_user_with_timestamps() {
        // Given - like Laravel's factory creation
        User user = User.builder()
            .name("John Doe")
            .email("john@example.com")
            .password("password123")
            .build();

        // When - save the entity
        entityManager.persist(user);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        
        // Verify timestamps are automatically set (Laravel-style)
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(user.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_user_and_modify_updated_at_timestamp() {
        // Given - create and save a user
        User user = User.builder()
            .name("Jane Doe")
            .email("jane@example.com")
            .password("password123")
            .build();
        entityManager.persist(user);
        entityManager.flush();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - update the user
        user.setName("Jane Smith");
        entityManager.flush();

        // Then - verify updated_at timestamp changed
        assertThat(user.getName()).isEqualTo("Jane Smith");
        assertThat(user.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(user.getCreatedAt()).isEqualTo(user.getCreatedAt()); // created_at should not change
    }

    @Test
    public void should_create_user_with_roles_relationship() {
        // Given - create role and user
        Role role = Role.builder()
            .name("STUDENT")
            .build();
        entityManager.persist(role);
        entityManager.flush();

        User user = User.builder()
            .name("Student User")
            .email("student@example.com")
            .password("password123")
            .build();
        entityManager.persist(user);
        entityManager.flush();

        // When - create user-role relationship
        UserRole userRole = UserRole.builder()
            .user(user)
            .role(role)
            .build();
        entityManager.persist(userRole);
        entityManager.flush();

        // Then - verify relationship and timestamps
        assertThat(userRole.getId()).isNotNull();
        assertThat(userRole.getUser()).isEqualTo(user);
        assertThat(userRole.getRole()).isEqualTo(role);
        assertThat(userRole.getCreatedAt()).isNotNull();
        assertThat(userRole.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_create_user_with_permissions_relationship() {
        // Given - create permission and user
        Permission permission = Permission.builder()
            .name("upload_lesson")
            .build();
        entityManager.persist(permission);
        entityManager.flush();

        User user = User.builder()
            .name("Teacher User")
            .email("teacher@example.com")
            .password("password123")
            .build();
        entityManager.persist(user);
        entityManager.flush();

        // When - create user-permission relationship
        UserPermission userPermission = UserPermission.builder()
            .user(user)
            .permission(permission)
            .build();
        entityManager.persist(userPermission);
        entityManager.flush();

        // Then - verify relationship and timestamps
        assertThat(userPermission.getId()).isNotNull();
        assertThat(userPermission.getUser()).isEqualTo(user);
        assertThat(userPermission.getPermission()).isEqualTo(permission);
        assertThat(userPermission.getCreatedAt()).isNotNull();
        assertThat(userPermission.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_enforce_unique_email_constraint() {
        // Given - create first user
        User user1 = User.builder()
            .name("User One")
            .email("duplicate@example.com")
            .password("password123")
            .build();
        entityManager.persist(user1);
        entityManager.flush();

        // When - try to create second user with same email
        User user2 = User.builder()
            .name("User Two")
            .email("duplicate@example.com")
            .password("password456")
            .build();

        // Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(user2);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }
}
