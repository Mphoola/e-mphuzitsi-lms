package com.mphoola.e_empuzitsi.config;

import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.repository.PermissionRepository;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer Tests")
class DataInitializerTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("Should initialize permissions when they don't exist")
    void should_initialize_permissions_when_they_dont_exist() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(false);
        
        Permission mockPermission = Permission.builder()
                .name("upload_lesson")
                .build();
        when(permissionRepository.save(any(Permission.class))).thenReturn(mockPermission);
        
        // When
        dataInitializer.run();
        
        // Then
        verify(permissionRepository, times(13)).existsByName(anyString());
        verify(permissionRepository, times(13)).save(any(Permission.class));
        
        // Verify specific permissions are checked
        verify(permissionRepository).existsByName("upload_lesson");
        verify(permissionRepository).existsByName("create_quiz");
        verify(permissionRepository).existsByName("manage_users");
        verify(permissionRepository).existsByName("view_lessons");
    }

    @Test
    @DisplayName("Should not create permissions when they already exist")
    void should_not_create_permissions_when_they_exist() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(true);
        
        // When
        dataInitializer.run();
        
        // Then
        verify(permissionRepository, times(13)).existsByName(anyString());
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    @Test
    @DisplayName("Should initialize STUDENT role with correct permissions")
    void should_initialize_student_role_with_correct_permissions() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(true);
        when(roleRepository.existsByName("STUDENT")).thenReturn(false);
        when(roleRepository.existsByName("TEACHER")).thenReturn(true);
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);
        
        // Mock student permissions
        Permission takeQuiz = Permission.builder().name("take_quiz").build();
        Permission viewLessons = Permission.builder().name("view_lessons").build();
        Permission participateDiscussion = Permission.builder().name("participate_discussion").build();
        
        when(permissionRepository.findByName("take_quiz")).thenReturn(Optional.of(takeQuiz));
        when(permissionRepository.findByName("view_lessons")).thenReturn(Optional.of(viewLessons));
        when(permissionRepository.findByName("participate_discussion")).thenReturn(Optional.of(participateDiscussion));
        
        Role mockStudentRole = Role.builder().name("STUDENT").build();
        when(roleRepository.save(any(Role.class))).thenReturn(mockStudentRole);
        
        // When
        dataInitializer.run();
        
        // Then
        verify(roleRepository).existsByName("STUDENT");
        verify(roleRepository).save(argThat(role -> 
            role.getName().equals("STUDENT") && role.getPermissions().size() == 3
        ));
        
        // Verify student permissions are retrieved
        verify(permissionRepository).findByName("take_quiz");
        verify(permissionRepository).findByName("view_lessons");
        verify(permissionRepository).findByName("participate_discussion");
    }

    @Test
    @DisplayName("Should initialize TEACHER role with correct permissions")
    void should_initialize_teacher_role_with_correct_permissions() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(true);
        when(roleRepository.existsByName("STUDENT")).thenReturn(true);
        when(roleRepository.existsByName("TEACHER")).thenReturn(false);
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);
        
        // Mock teacher permissions
        List<String> teacherPermissions = Arrays.asList(
            "upload_lesson", "create_quiz", "grade_quiz", "manage_students",
            "view_reports", "manage_subjects", "view_lessons", "participate_discussion", "take_quiz"
        );
        
        for (String permName : teacherPermissions) {
            Permission perm = Permission.builder().name(permName).build();
            when(permissionRepository.findByName(permName)).thenReturn(Optional.of(perm));
        }
        
        Role mockTeacherRole = Role.builder().name("TEACHER").build();
        when(roleRepository.save(any(Role.class))).thenReturn(mockTeacherRole);
        
        // When
        dataInitializer.run();
        
        // Then
        verify(roleRepository).existsByName("TEACHER");
        verify(roleRepository).save(argThat(role -> 
            role.getName().equals("TEACHER") && role.getPermissions().size() == 9
        ));
        
        // Verify some teacher permissions are retrieved
        verify(permissionRepository).findByName("upload_lesson");
        verify(permissionRepository).findByName("create_quiz");
        verify(permissionRepository).findByName("manage_students");
    }

    @Test
    @DisplayName("Should initialize ADMIN role with all permissions")
    void should_initialize_admin_role_with_all_permissions() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(true);
        when(roleRepository.existsByName("STUDENT")).thenReturn(true);
        when(roleRepository.existsByName("TEACHER")).thenReturn(true);
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        
        // Mock all permissions for admin
        List<Permission> allPermissions = Arrays.asList(
            Permission.builder().name("upload_lesson").build(),
            Permission.builder().name("create_quiz").build(),
            Permission.builder().name("manage_users").build(),
            Permission.builder().name("manage_roles").build(),
            Permission.builder().name("view_analytics").build()
        );
        
        when(permissionRepository.findAll()).thenReturn(allPermissions);
        
        Role mockAdminRole = Role.builder().name("ADMIN").build();
        when(roleRepository.save(any(Role.class))).thenReturn(mockAdminRole);
        
        // When
        dataInitializer.run();
        
        // Then
        verify(roleRepository).existsByName("ADMIN");
        verify(permissionRepository).findAll();
        verify(roleRepository).save(argThat(role -> 
            role.getName().equals("ADMIN") && role.getPermissions().size() == 5
        ));
    }

    @Test
    @DisplayName("Should not create roles when they already exist")
    void should_not_create_roles_when_they_exist() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(true);
        when(roleRepository.existsByName(anyString())).thenReturn(true);
        
        // When
        dataInitializer.run();
        
        // Then
        verify(roleRepository).existsByName("STUDENT");
        verify(roleRepository).existsByName("TEACHER");
        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should handle missing permissions gracefully")
    void should_handle_missing_permissions_gracefully() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(true);
        when(roleRepository.existsByName("STUDENT")).thenReturn(false);
        when(roleRepository.existsByName("TEACHER")).thenReturn(true);
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);
        
        // Mock that some permissions are not found
        when(permissionRepository.findByName("take_quiz")).thenReturn(Optional.empty());
        when(permissionRepository.findByName("view_lessons")).thenReturn(Optional.of(
            Permission.builder().name("view_lessons").build()
        ));
        when(permissionRepository.findByName("participate_discussion")).thenReturn(Optional.of(
            Permission.builder().name("participate_discussion").build()
        ));
        
        Role mockStudentRole = Role.builder().name("STUDENT").build();
        when(roleRepository.save(any(Role.class))).thenReturn(mockStudentRole);
        
        // When
        dataInitializer.run();
        
        // Then - Should still create role with available permissions
        verify(roleRepository).save(argThat(role -> 
            role.getName().equals("STUDENT") && role.getPermissions().size() == 2
        ));
    }

    @Test
    @DisplayName("Should complete initialization successfully")
    void should_complete_initialization_successfully() throws Exception {
        // Given
        when(permissionRepository.existsByName(anyString())).thenReturn(false);
        when(roleRepository.existsByName(anyString())).thenReturn(false);
        
        // Mock permission creation
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission perm = invocation.getArgument(0);
            return Permission.builder().name(perm.getName()).build();
        });
        
        // Mock role creation  
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock permission lookups for roles
        when(permissionRepository.findByName(anyString())).thenReturn(Optional.of(
            Permission.builder().name("test_permission").build()
        ));
        when(permissionRepository.findAll()).thenReturn(Arrays.asList(
            Permission.builder().name("test_permission").build()
        ));
        
        // When & Then - Should not throw exception
        dataInitializer.run();
        
        // Verify the process completes
        verify(permissionRepository, times(13)).save(any(Permission.class));
        verify(roleRepository, times(3)).save(any(Role.class));
    }
}
