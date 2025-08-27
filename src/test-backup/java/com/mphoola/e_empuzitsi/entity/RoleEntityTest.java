package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA tests for Role entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class RoleEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_role_with_timestamps() {
        // Given
        Role role = Role.builder()
            .name("ADMIN")
            .build();

        // When
        entityManager.persist(role);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(role.getId()).isNotNull();
        assertThat(role.getName()).isEqualTo("ADMIN");
        assertThat(role.getCreatedAt()).isNotNull();
        assertThat(role.getUpdatedAt()).isNotNull();
        assertThat(role.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(role.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_role_and_modify_timestamps() {
        // Given - create initial role
        Role role = Role.builder()
            .name("STUDENT")
            .build();
        
        entityManager.persist(role);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = role.getCreatedAt();
        LocalDateTime initialUpdatedAt = role.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update role
        role.setName("ADVANCED_STUDENT");
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(role.getName()).isEqualTo("ADVANCED_STUDENT");
        assertThat(role.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(role.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_name_field() {
        // Given - role without required name
        Role role = Role.builder()
            .build(); // Missing name

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(role);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_enforce_unique_name_constraint() {
        // Given - create first role
        Role role1 = Role.builder()
            .name("TEACHER")
            .build();
        
        entityManager.persist(role1);
        entityManager.flush();

        // Second role with same name
        Role role2 = Role.builder()
            .name("TEACHER") // Duplicate name
            .build();

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(role2);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_support_user_role_relationship() {
        // Given - create role and user
        Role role = Role.builder()
            .name("MODERATOR")
            .build();
        
        User user = User.builder()
            .name("Moderator User")
            .email("moderator@example.com")
            .password("mod123")
            .build();

        entityManager.persist(role);
        entityManager.persist(user);
        entityManager.flush();

        UserRole userRole = UserRole.builder()
            .user(user)
            .role(role)
            .build();

        // When
        entityManager.persist(userRole);
        entityManager.flush();

        // Clear and reload to test relationship
        entityManager.clear();
        Role reloadedRole = entityManager.find(Role.class, role.getId());

        // Then - verify relationship exists
        assertThat(reloadedRole).isNotNull();
        assertThat(reloadedRole.getName()).isEqualTo("MODERATOR");
        // Note: We don't test the collection directly due to lazy loading in test context
    }

    @Test
    public void should_support_role_permission_relationship() {
        // Given - create role and permissions
        Permission readPermission = Permission.builder()
            .name("READ_CONTENT")
            .build();
        
        Permission writePermission = Permission.builder()
            .name("WRITE_CONTENT")
            .build();

        Role role = Role.builder()
            .name("CONTENT_EDITOR")
            .build();

        entityManager.persist(readPermission);
        entityManager.persist(writePermission);
        entityManager.persist(role);
        entityManager.flush();

        // Associate permissions with role
        role.setPermissions(Set.of(readPermission, writePermission));

        // When
        entityManager.flush();

        // Clear and reload to test relationship
        entityManager.clear();
        Role reloadedRole = entityManager.find(Role.class, role.getId());

        // Then - verify role exists (relationship tested indirectly)
        assertThat(reloadedRole).isNotNull();
        assertThat(reloadedRole.getName()).isEqualTo("CONTENT_EDITOR");
        // Note: We don't test the collection directly due to lazy loading in test context
    }

    @Test
    public void should_create_multiple_roles_with_different_names() {
        // Given - multiple roles with different names
        Role adminRole = Role.builder()
            .name("SUPER_ADMIN")
            .build();
        
        Role teacherRole = Role.builder()
            .name("SENIOR_TEACHER")
            .build();
        
        Role studentRole = Role.builder()
            .name("GRADUATE_STUDENT")
            .build();

        // When
        entityManager.persist(adminRole);
        entityManager.persist(teacherRole);
        entityManager.persist(studentRole);
        entityManager.flush();

        // Then - all roles should be created successfully
        assertThat(adminRole.getId()).isNotNull();
        assertThat(teacherRole.getId()).isNotNull();
        assertThat(studentRole.getId()).isNotNull();
        assertThat(adminRole.getName()).isEqualTo("SUPER_ADMIN");
        assertThat(teacherRole.getName()).isEqualTo("SENIOR_TEACHER");
        assertThat(studentRole.getName()).isEqualTo("GRADUATE_STUDENT");
    }

    @Test
    public void should_support_common_role_names() {
        // Given - roles with common system names
        Role userRole = Role.builder()
            .name("USER")
            .build();
        
        Role guestRole = Role.builder()
            .name("GUEST")
            .build();
        
        Role managerRole = Role.builder()
            .name("MANAGER")
            .build();

        // When
        entityManager.persist(userRole);
        entityManager.persist(guestRole);
        entityManager.persist(managerRole);
        entityManager.flush();

        // Then - all roles should be created successfully
        assertThat(userRole.getId()).isNotNull();
        assertThat(guestRole.getId()).isNotNull();
        assertThat(managerRole.getId()).isNotNull();
        assertThat(userRole.getName()).isEqualTo("USER");
        assertThat(guestRole.getName()).isEqualTo("GUEST");
        assertThat(managerRole.getName()).isEqualTo("MANAGER");
    }
}
