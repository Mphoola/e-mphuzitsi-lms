package com.mphoola.e_empuzitsi.config;

import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.repository.PermissionRepository;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Transactional
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    public DataInitializer(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default roles and permissions...");
        
        // Initialize permissions
        initializePermissions();
        
        // Initialize roles
        initializeRoles();
        
        log.info("Data initialization completed successfully!");
    }
    
    private void initializePermissions() {
        List<String> permissionNames = Arrays.asList(
            "upload_lesson",
            "create_quiz",
            "grade_quiz",
            "manage_students",
            "view_reports",
            "manage_subjects",
            "manage_users",
            "manage_roles",
            "manage_permissions",
            "view_analytics",
            "take_quiz",
            "view_lessons",
            "participate_discussion"
        );
        
        for (String permissionName : permissionNames) {
            if (!permissionRepository.existsByName(permissionName)) {
                Permission permission = Permission.builder()
                        .name(permissionName)
                        .build();
                
                permissionRepository.save(permission);
                log.debug("Created permission: {}", permissionName);
            }
        }
    }
    
    private void initializeRoles() {
        // Initialize STUDENT role
        initializeStudentRole();
        
        // Initialize TEACHER role
        initializeTeacherRole();
        
        // Initialize ADMIN role
        initializeAdminRole();
    }
    
    private void initializeStudentRole() {
        if (!roleRepository.existsByName("STUDENT")) {
            Set<Permission> studentPermissions = new HashSet<>();
            
            // Student permissions
            List<String> studentPermissionNames = Arrays.asList(
                "take_quiz",
                "view_lessons",
                "participate_discussion"
            );
            
            for (String permissionName : studentPermissionNames) {
                permissionRepository.findByName(permissionName)
                        .ifPresent(studentPermissions::add);
            }
            
            Role studentRole = Role.builder()
                    .name("STUDENT")
                    .permissions(studentPermissions)
                    .build();
            
            roleRepository.save(studentRole);
            log.info("Created STUDENT role with {} permissions", studentPermissions.size());
        }
    }
    
    private void initializeTeacherRole() {
        if (!roleRepository.existsByName("TEACHER")) {
            Set<Permission> teacherPermissions = new HashSet<>();
            
            // Teacher permissions
            List<String> teacherPermissionNames = Arrays.asList(
                "upload_lesson",
                "create_quiz",
                "grade_quiz",
                "manage_students",
                "view_reports",
                "manage_subjects",
                "view_lessons",
                "participate_discussion",
                "take_quiz"
            );
            
            for (String permissionName : teacherPermissionNames) {
                permissionRepository.findByName(permissionName)
                        .ifPresent(teacherPermissions::add);
            }
            
            Role teacherRole = Role.builder()
                    .name("TEACHER")
                    .permissions(teacherPermissions)
                    .build();
            
            roleRepository.save(teacherRole);
            log.info("Created TEACHER role with {} permissions", teacherPermissions.size());
        }
    }
    
    private void initializeAdminRole() {
        if (!roleRepository.existsByName("ADMIN")) {
            Set<Permission> adminPermissions = new HashSet<>();
            
            // Admin gets all permissions
            List<Permission> allPermissions = permissionRepository.findAll();
            adminPermissions.addAll(allPermissions);
            
            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .permissions(adminPermissions)
                    .build();
            
            roleRepository.save(adminRole);
            log.info("Created ADMIN role with {} permissions", adminPermissions.size());
        }
    }
}
