package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA tests for UserRole entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserRoleEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_user_role_with_timestamps() {
        // Given - create required dependencies
        User user = User.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .password("password123")
            .build();
        
        Role role = Role.builder()
            .name("STUDENT")
            .build();

        entityManager.persist(user);
        entityManager.persist(role);
        entityManager.flush();

        UserRole userRole = UserRole.builder()
            .user(user)
            .role(role)
            .build();

        // When
        entityManager.persist(userRole);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(userRole.getId()).isNotNull();
        assertThat(userRole.getUser()).isEqualTo(user);
        assertThat(userRole.getRole()).isEqualTo(role);
        assertThat(userRole.getCreatedAt()).isNotNull();
        assertThat(userRole.getUpdatedAt()).isNotNull();
        assertThat(userRole.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(userRole.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_user_role_and_modify_timestamps() {
        // Given - create user role
        User user = User.builder()
            .name("Jane Smith")
            .email("jane.smith@example.com")
            .password("password123")
            .build();
        
        Role oldRole = Role.builder()
            .name("TEACHER")
            .build();
        
        Role newRole = Role.builder()
            .name("ADMIN")
            .build();

        UserRole userRole = UserRole.builder()
            .user(user)
            .role(oldRole)
            .build();

        entityManager.persist(user);
        entityManager.persist(oldRole);
        entityManager.persist(newRole);
        entityManager.persist(userRole);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = userRole.getCreatedAt();
        LocalDateTime initialUpdatedAt = userRole.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update role
        userRole.setRole(newRole);
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(userRole.getRole()).isEqualTo(newRole);
        assertThat(userRole.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(userRole.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_user_relationship() {
        // Given - create role
        Role role = Role.builder()
            .name("MODERATOR")
            .build();

        entityManager.persist(role);
        entityManager.flush();

        // UserRole without required user
        UserRole userRole = UserRole.builder()
            .role(role)
            .build(); // Missing user relationship

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(userRole);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_role_relationship() {
        // Given - create user
        User user = User.builder()
            .name("Alice Johnson")
            .email("alice.johnson@example.com")
            .password("password123")
            .build();

        entityManager.persist(user);
        entityManager.flush();

        // UserRole without required role
        UserRole userRole = UserRole.builder()
            .user(user)
            .build(); // Missing role relationship

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(userRole);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_enforce_unique_user_role_constraint() {
        // Given - create user and role
        User user = User.builder()
            .name("Bob Wilson")
            .email("bob.wilson@example.com")
            .password("password123")
            .build();
        
        Role role = Role.builder()
            .name("EDITOR")
            .build();

        entityManager.persist(user);
        entityManager.persist(role);
        entityManager.flush();

        // Create first user-role association
        UserRole userRole1 = UserRole.builder()
            .user(user)
            .role(role)
            .build();
        
        entityManager.persist(userRole1);
        entityManager.flush();

        // Try to create duplicate user-role association
        UserRole userRole2 = UserRole.builder()
            .user(user)
            .role(role) // Same user and role
            .build();

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(userRole2);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_allow_same_user_with_different_roles() {
        // Given - create user and multiple roles
        User user = User.builder()
            .name("Charlie Brown")
            .email("charlie.brown@example.com")
            .password("password123")
            .build();
        
        Role role1 = Role.builder()
            .name("STUDENT")
            .build();
        
        Role role2 = Role.builder()
            .name("TUTOR")
            .build();

        entityManager.persist(user);
        entityManager.persist(role1);
        entityManager.persist(role2);
        entityManager.flush();

        // Create user-role associations with same user but different roles
        UserRole userRole1 = UserRole.builder()
            .user(user)
            .role(role1)
            .build();
        
        UserRole userRole2 = UserRole.builder()
            .user(user)
            .role(role2)
            .build();

        // When
        entityManager.persist(userRole1);
        entityManager.persist(userRole2);
        entityManager.flush();

        // Then - both associations should be created successfully
        assertThat(userRole1.getId()).isNotNull();
        assertThat(userRole2.getId()).isNotNull();
        assertThat(userRole1.getUser()).isEqualTo(user);
        assertThat(userRole2.getUser()).isEqualTo(user);
        assertThat(userRole1.getRole()).isEqualTo(role1);
        assertThat(userRole2.getRole()).isEqualTo(role2);
    }

    @Test
    public void should_allow_same_role_with_different_users() {
        // Given - create multiple users and one role
        User user1 = User.builder()
            .name("David Lee")
            .email("david.lee@example.com")
            .password("password123")
            .build();
        
        User user2 = User.builder()
            .name("Eva Garcia")
            .email("eva.garcia@example.com")
            .password("password123")
            .build();
        
        Role role = Role.builder()
            .name("CONTRIBUTOR")
            .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(role);
        entityManager.flush();

        // Create user-role associations with different users but same role
        UserRole userRole1 = UserRole.builder()
            .user(user1)
            .role(role)
            .build();
        
        UserRole userRole2 = UserRole.builder()
            .user(user2)
            .role(role)
            .build();

        // When
        entityManager.persist(userRole1);
        entityManager.persist(userRole2);
        entityManager.flush();

        // Then - both associations should be created successfully
        assertThat(userRole1.getId()).isNotNull();
        assertThat(userRole2.getId()).isNotNull();
        assertThat(userRole1.getUser()).isEqualTo(user1);
        assertThat(userRole2.getUser()).isEqualTo(user2);
        assertThat(userRole1.getRole()).isEqualTo(role);
        assertThat(userRole2.getRole()).isEqualTo(role);
    }

    @Test
    public void should_support_multiple_user_role_associations() {
        // Given - create multiple users and roles
        User admin = User.builder()
            .name("Admin User")
            .email("admin@example.com")
            .password("admin123")
            .build();
        
        User teacher = User.builder()
            .name("Teacher User")
            .email("teacher@example.com")
            .password("teacher123")
            .build();
        
        Role adminRole = Role.builder()
            .name("ADMIN")
            .build();
        
        Role teacherRole = Role.builder()
            .name("TEACHER")
            .build();
        
        Role userRole = Role.builder()
            .name("USER")
            .build();

        entityManager.persist(admin);
        entityManager.persist(teacher);
        entityManager.persist(adminRole);
        entityManager.persist(teacherRole);
        entityManager.persist(userRole);
        entityManager.flush();

        // Create multiple associations
        UserRole adminAdminRole = UserRole.builder()
            .user(admin)
            .role(adminRole)
            .build();
        
        UserRole adminUserRole = UserRole.builder()
            .user(admin)
            .role(userRole)
            .build();
        
        UserRole teacherTeacherRole = UserRole.builder()
            .user(teacher)
            .role(teacherRole)
            .build();
        
        UserRole teacherUserRole = UserRole.builder()
            .user(teacher)
            .role(userRole)
            .build();

        // When
        entityManager.persist(adminAdminRole);
        entityManager.persist(adminUserRole);
        entityManager.persist(teacherTeacherRole);
        entityManager.persist(teacherUserRole);
        entityManager.flush();

        // Then - all associations should be created successfully
        assertThat(adminAdminRole.getId()).isNotNull();
        assertThat(adminUserRole.getId()).isNotNull();
        assertThat(teacherTeacherRole.getId()).isNotNull();
        assertThat(teacherUserRole.getId()).isNotNull();
    }
}
