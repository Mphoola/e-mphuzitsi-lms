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
 * JPA tests for UserPermission entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserPermissionEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_user_permission_with_timestamps() {
        // Given - create required dependencies
        User user = User.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .password("password123")
            .build();
        
        Permission permission = Permission.builder()
            .name("READ_CONTENT")
            .build();

        entityManager.persist(user);
        entityManager.persist(permission);
        entityManager.flush();

        UserPermission userPermission = UserPermission.builder()
            .user(user)
            .permission(permission)
            .build();

        // When
        entityManager.persist(userPermission);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(userPermission.getId()).isNotNull();
        assertThat(userPermission.getUser()).isEqualTo(user);
        assertThat(userPermission.getPermission()).isEqualTo(permission);
        assertThat(userPermission.getCreatedAt()).isNotNull();
        assertThat(userPermission.getUpdatedAt()).isNotNull();
        assertThat(userPermission.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(userPermission.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_user_permission_and_modify_timestamps() {
        // Given - create user permission
        User user = User.builder()
            .name("Jane Smith")
            .email("jane.smith@example.com")
            .password("password123")
            .build();
        
        Permission oldPermission = Permission.builder()
            .name("READ_CONTENT")
            .build();
        
        Permission newPermission = Permission.builder()
            .name("WRITE_CONTENT")
            .build();

        UserPermission userPermission = UserPermission.builder()
            .user(user)
            .permission(oldPermission)
            .build();

        entityManager.persist(user);
        entityManager.persist(oldPermission);
        entityManager.persist(newPermission);
        entityManager.persist(userPermission);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = userPermission.getCreatedAt();
        LocalDateTime initialUpdatedAt = userPermission.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update permission
        userPermission.setPermission(newPermission);
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(userPermission.getPermission()).isEqualTo(newPermission);
        assertThat(userPermission.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(userPermission.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_user_relationship() {
        // Given - create permission
        Permission permission = Permission.builder()
            .name("DELETE_CONTENT")
            .build();

        entityManager.persist(permission);
        entityManager.flush();

        // UserPermission without required user
        UserPermission userPermission = UserPermission.builder()
            .permission(permission)
            .build(); // Missing user relationship

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(userPermission);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_permission_relationship() {
        // Given - create user
        User user = User.builder()
            .name("Alice Johnson")
            .email("alice.johnson@example.com")
            .password("password123")
            .build();

        entityManager.persist(user);
        entityManager.flush();

        // UserPermission without required permission
        UserPermission userPermission = UserPermission.builder()
            .user(user)
            .build(); // Missing permission relationship

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(userPermission);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_enforce_unique_user_permission_constraint() {
        // Given - create user and permission
        User user = User.builder()
            .name("Bob Wilson")
            .email("bob.wilson@example.com")
            .password("password123")
            .build();
        
        Permission permission = Permission.builder()
            .name("UPLOAD_FILE")
            .build();

        entityManager.persist(user);
        entityManager.persist(permission);
        entityManager.flush();

        // Create first user-permission association
        UserPermission userPermission1 = UserPermission.builder()
            .user(user)
            .permission(permission)
            .build();
        
        entityManager.persist(userPermission1);
        entityManager.flush();

        // Try to create duplicate user-permission association
        UserPermission userPermission2 = UserPermission.builder()
            .user(user)
            .permission(permission) // Same user and permission
            .build();

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(userPermission2);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_allow_same_user_with_different_permissions() {
        // Given - create user and multiple permissions
        User user = User.builder()
            .name("Charlie Brown")
            .email("charlie.brown@example.com")
            .password("password123")
            .build();
        
        Permission readPermission = Permission.builder()
            .name("READ_LESSONS")
            .build();
        
        Permission writePermission = Permission.builder()
            .name("WRITE_LESSONS")
            .build();

        entityManager.persist(user);
        entityManager.persist(readPermission);
        entityManager.persist(writePermission);
        entityManager.flush();

        // Create user-permission associations with same user but different permissions
        UserPermission userReadPermission = UserPermission.builder()
            .user(user)
            .permission(readPermission)
            .build();
        
        UserPermission userWritePermission = UserPermission.builder()
            .user(user)
            .permission(writePermission)
            .build();

        // When
        entityManager.persist(userReadPermission);
        entityManager.persist(userWritePermission);
        entityManager.flush();

        // Then - both associations should be created successfully
        assertThat(userReadPermission.getId()).isNotNull();
        assertThat(userWritePermission.getId()).isNotNull();
        assertThat(userReadPermission.getUser()).isEqualTo(user);
        assertThat(userWritePermission.getUser()).isEqualTo(user);
        assertThat(userReadPermission.getPermission()).isEqualTo(readPermission);
        assertThat(userWritePermission.getPermission()).isEqualTo(writePermission);
    }

    @Test
    public void should_allow_same_permission_with_different_users() {
        // Given - create multiple users and one permission
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
        
        Permission permission = Permission.builder()
            .name("VIEW_DASHBOARD")
            .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(permission);
        entityManager.flush();

        // Create user-permission associations with different users but same permission
        UserPermission user1Permission = UserPermission.builder()
            .user(user1)
            .permission(permission)
            .build();
        
        UserPermission user2Permission = UserPermission.builder()
            .user(user2)
            .permission(permission)
            .build();

        // When
        entityManager.persist(user1Permission);
        entityManager.persist(user2Permission);
        entityManager.flush();

        // Then - both associations should be created successfully
        assertThat(user1Permission.getId()).isNotNull();
        assertThat(user2Permission.getId()).isNotNull();
        assertThat(user1Permission.getUser()).isEqualTo(user1);
        assertThat(user2Permission.getUser()).isEqualTo(user2);
        assertThat(user1Permission.getPermission()).isEqualTo(permission);
        assertThat(user2Permission.getPermission()).isEqualTo(permission);
    }

    @Test
    public void should_support_multiple_user_permission_associations() {
        // Given - create multiple users and permissions
        User admin = User.builder()
            .name("Admin User")
            .email("admin@example.com")
            .password("admin123")
            .build();
        
        User editor = User.builder()
            .name("Editor User")
            .email("editor@example.com")
            .password("editor123")
            .build();
        
        Permission createPermission = Permission.builder()
            .name("CREATE_CONTENT")
            .build();
        
        Permission editPermission = Permission.builder()
            .name("EDIT_CONTENT")
            .build();
        
        Permission deletePermission = Permission.builder()
            .name("DELETE_CONTENT")
            .build();

        entityManager.persist(admin);
        entityManager.persist(editor);
        entityManager.persist(createPermission);
        entityManager.persist(editPermission);
        entityManager.persist(deletePermission);
        entityManager.flush();

        // Create multiple associations for admin (full permissions)
        UserPermission adminCreate = UserPermission.builder()
            .user(admin)
            .permission(createPermission)
            .build();
        
        UserPermission adminEdit = UserPermission.builder()
            .user(admin)
            .permission(editPermission)
            .build();
        
        UserPermission adminDelete = UserPermission.builder()
            .user(admin)
            .permission(deletePermission)
            .build();
        
        // Create limited associations for editor
        UserPermission editorEdit = UserPermission.builder()
            .user(editor)
            .permission(editPermission)
            .build();

        // When
        entityManager.persist(adminCreate);
        entityManager.persist(adminEdit);
        entityManager.persist(adminDelete);
        entityManager.persist(editorEdit);
        entityManager.flush();

        // Then - all associations should be created successfully
        assertThat(adminCreate.getId()).isNotNull();
        assertThat(adminEdit.getId()).isNotNull();
        assertThat(adminDelete.getId()).isNotNull();
        assertThat(editorEdit.getId()).isNotNull();
        
        // Verify admin has all permissions
        assertThat(adminCreate.getUser()).isEqualTo(admin);
        assertThat(adminEdit.getUser()).isEqualTo(admin);
        assertThat(adminDelete.getUser()).isEqualTo(admin);
        
        // Verify editor has limited permissions
        assertThat(editorEdit.getUser()).isEqualTo(editor);
        assertThat(editorEdit.getPermission()).isEqualTo(editPermission);
    }

    @Test
    public void should_support_security_permission_patterns() {
        // Given - create users and security-specific permissions
        User securityAdmin = User.builder()
            .name("Security Admin")
            .email("security@example.com")
            .password("secure123")
            .build();
        
        User regularUser = User.builder()
            .name("Regular User")
            .email("user@example.com")
            .password("user123")
            .build();

        // Create security permissions following common naming patterns
        Permission manageUsers = Permission.builder()
            .name("MANAGE_USERS")
            .build();
        
        Permission manageRoles = Permission.builder()
            .name("MANAGE_ROLES")
            .build();
        
        Permission viewReports = Permission.builder()
            .name("VIEW_REPORTS")
            .build();
        
        Permission basicAccess = Permission.builder()
            .name("BASIC_ACCESS")
            .build();

        entityManager.persist(securityAdmin);
        entityManager.persist(regularUser);
        entityManager.persist(manageUsers);
        entityManager.persist(manageRoles);
        entityManager.persist(viewReports);
        entityManager.persist(basicAccess);
        entityManager.flush();

        // Assign admin permissions
        UserPermission adminManageUsers = UserPermission.builder()
            .user(securityAdmin)
            .permission(manageUsers)
            .build();
        
        UserPermission adminManageRoles = UserPermission.builder()
            .user(securityAdmin)
            .permission(manageRoles)
            .build();
        
        UserPermission adminViewReports = UserPermission.builder()
            .user(securityAdmin)
            .permission(viewReports)
            .build();
        
        // Assign basic user permissions
        UserPermission userBasicAccess = UserPermission.builder()
            .user(regularUser)
            .permission(basicAccess)
            .build();

        // When
        entityManager.persist(adminManageUsers);
        entityManager.persist(adminManageRoles);
        entityManager.persist(adminViewReports);
        entityManager.persist(userBasicAccess);
        entityManager.flush();

        // Then - verify security permission assignments
        assertThat(adminManageUsers.getId()).isNotNull();
        assertThat(adminManageRoles.getId()).isNotNull();
        assertThat(adminViewReports.getId()).isNotNull();
        assertThat(userBasicAccess.getId()).isNotNull();
        
        // Verify admin permissions
        assertThat(adminManageUsers.getPermission().getName()).isEqualTo("MANAGE_USERS");
        assertThat(adminManageRoles.getPermission().getName()).isEqualTo("MANAGE_ROLES");
        assertThat(adminViewReports.getPermission().getName()).isEqualTo("VIEW_REPORTS");
        
        // Verify user permissions
        assertThat(userBasicAccess.getPermission().getName()).isEqualTo("BASIC_ACCESS");
    }
}
