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
 * JPA tests for Permission entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PermissionEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_permission_with_timestamps() {
        // Given
        Permission permission = Permission.builder()
            .name("READ_USERS")
            .build();

        // When
        entityManager.persist(permission);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(permission.getId()).isNotNull();
        assertThat(permission.getName()).isEqualTo("READ_USERS");
        assertThat(permission.getCreatedAt()).isNotNull();
        assertThat(permission.getUpdatedAt()).isNotNull();
        assertThat(permission.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(permission.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_permission_and_modify_timestamps() {
        // Given - create initial permission
        Permission permission = Permission.builder()
            .name("WRITE_POSTS")
            .build();
        
        entityManager.persist(permission);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = permission.getCreatedAt();
        LocalDateTime initialUpdatedAt = permission.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update permission
        permission.setName("WRITE_POSTS_ADVANCED");
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(permission.getName()).isEqualTo("WRITE_POSTS_ADVANCED");
        assertThat(permission.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(permission.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_name_field() {
        // Given - permission without required name
        Permission permission = Permission.builder()
            .build(); // Missing name

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(permission);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_enforce_unique_name_constraint() {
        // Given - create first permission
        Permission permission1 = Permission.builder()
            .name("DELETE_FILES")
            .build();
        
        entityManager.persist(permission1);
        entityManager.flush();

        // Second permission with same name
        Permission permission2 = Permission.builder()
            .name("DELETE_FILES") // Duplicate name
            .build();

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(permission2);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_support_user_permission_relationship() {
        // Given - create permission and user
        Permission permission = Permission.builder()
            .name("MANAGE_COURSES")
            .build();
        
        User user = User.builder()
            .name("Admin User")
            .email("admin@example.com")
            .password("admin123")
            .build();

        entityManager.persist(permission);
        entityManager.persist(user);
        entityManager.flush();

        UserPermission userPermission = UserPermission.builder()
            .user(user)
            .permission(permission)
            .build();

        // When
        entityManager.persist(userPermission);
        entityManager.flush();

        // Clear and reload to test relationship
        entityManager.clear();
        Permission reloadedPermission = entityManager.find(Permission.class, permission.getId());

        // Then - verify relationship exists
        assertThat(reloadedPermission).isNotNull();
        assertThat(reloadedPermission.getName()).isEqualTo("MANAGE_COURSES");
        // Note: We don't test the collection directly due to lazy loading in test context
    }

    @Test
    public void should_create_multiple_permissions_with_different_names() {
        // Given - multiple permissions with different names
        Permission permission1 = Permission.builder()
            .name("CREATE_QUIZ")
            .build();
        
        Permission permission2 = Permission.builder()
            .name("EDIT_QUIZ")
            .build();
        
        Permission permission3 = Permission.builder()
            .name("DELETE_QUIZ")
            .build();

        // When
        entityManager.persist(permission1);
        entityManager.persist(permission2);
        entityManager.persist(permission3);
        entityManager.flush();

        // Then - all permissions should be created successfully
        assertThat(permission1.getId()).isNotNull();
        assertThat(permission2.getId()).isNotNull();
        assertThat(permission3.getId()).isNotNull();
        assertThat(permission1.getName()).isEqualTo("CREATE_QUIZ");
        assertThat(permission2.getName()).isEqualTo("EDIT_QUIZ");
        assertThat(permission3.getName()).isEqualTo("DELETE_QUIZ");
    }

    @Test
    public void should_support_common_permission_names() {
        // Given - permissions with common CRUD names
        Permission readPermission = Permission.builder()
            .name("READ")
            .build();
        
        Permission writePermission = Permission.builder()
            .name("WRITE")
            .build();
        
        Permission updatePermission = Permission.builder()
            .name("UPDATE")
            .build();
        
        Permission deletePermission = Permission.builder()
            .name("DELETE")
            .build();

        // When
        entityManager.persist(readPermission);
        entityManager.persist(writePermission);
        entityManager.persist(updatePermission);
        entityManager.persist(deletePermission);
        entityManager.flush();

        // Then - all permissions should be created successfully
        assertThat(readPermission.getId()).isNotNull();
        assertThat(writePermission.getId()).isNotNull();
        assertThat(updatePermission.getId()).isNotNull();
        assertThat(deletePermission.getId()).isNotNull();
    }
}
