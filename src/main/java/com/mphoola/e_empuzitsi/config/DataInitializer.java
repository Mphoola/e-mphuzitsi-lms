package com.mphoola.e_empuzitsi.config;

import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.entity.UserRole;
import com.mphoola.e_empuzitsi.repository.PermissionRepository;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
@Transactional
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    public DataInitializer(RoleRepository roleRepository, PermissionRepository permissionRepository,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default roles and permissions...");
        
        // Initialize permissions first
        initializePermissions();
        
        // Initialize roles second (depends on permissions)
        initializeRoles();

        // Create default admin user last (depends on roles)
        createDefaultAdminUser();

        log.info("Data initialization completed successfully!");
    }
    
    private void createDefaultAdminUser() {
        String adminEmail = "admin@gmail.com";
        String adminPassword = "123456789";

        User adminUser;

        // Check if the admin user already exists
        if (!userRepository.existsByEmail(adminEmail)) {
            adminUser = User.builder()
                    .name("Admin User")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .build();

            // Save the user first to get the ID
            adminUser = userRepository.save(adminUser);
            log.info("Created new admin user: {}", adminEmail);
        } else {
            // Get existing admin user
            adminUser = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));
            log.info("Found existing admin user: {}", adminEmail);
        }

        // Get the ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        // Check if user already has ADMIN role
        boolean hasAdminRole = adminUser.getUserRoles() != null && 
                adminUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equals("ADMIN"));

        if (!hasAdminRole) {
            // Create UserRole association
            UserRole userRole = UserRole.builder()
                    .user(adminUser)
                    .role(adminRole)
                    .build();

            // Initialize userRoles collection if it's null
            if (adminUser.getUserRoles() == null) {
                adminUser.setUserRoles(new HashSet<>());
            }
            adminUser.getUserRoles().add(userRole);

            // Save the updated user with roles
            userRepository.save(adminUser);
            log.info("Assigned ADMIN role to user: {}", adminEmail);
        } else {
            log.info("Admin user {} already has ADMIN role", adminEmail);
        }
    }

    private void initializePermissions() {
        List<String> permissionNames = Arrays.asList(
            // Role management permissions
            "add_role",
            "update_role",
            "delete_role",
            "show_role_details",
            "list_roles",
            
            // Activity log permissions
            "list_audit_logs",
            "see_log_details",
            "list_user_audit_log",
            
            // User role and permission management
            "assign_user_role",
            "revoke_user_role",
            "list_user_roles",
            "assign_user_permission",
            "revoke_user_permission",
            "list_user_permissions",
            "manage_user_access"
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
        Role studentRole = null;
        if (roleRepository.existsByName("STUDENT")) {
            // Update existing STUDENT role with new permissions
            studentRole = roleRepository.findByName("STUDENT").get();
            studentRole.getPermissions().clear(); // Clear old permissions
        } else {
            // Create new STUDENT role
            studentRole = Role.builder()
                    .name("STUDENT")
                    .permissions(new HashSet<>())
                    .build();
        }

        // Save the STUDENT role
        roleRepository.save(studentRole);
        log.info("Created/Updated STUDENT role with {} permissions", studentRole.getPermissions().size());
    }
    
    private void initializeTeacherRole() {
        Role teacherRole = null;
        if (roleRepository.existsByName("TEACHER")) {
            // Update existing TEACHER role with new permissions
            teacherRole = roleRepository.findByName("TEACHER").get();
            teacherRole.getPermissions().clear(); // Clear old permissions
        } else {
            // Create new TEACHER role
            teacherRole = Role.builder()
                    .name("TEACHER")
                    .permissions(new HashSet<>())
                    .build();
        }

        // Save the TEACHER role
        roleRepository.save(teacherRole);
        log.info("Created/Updated TEACHER role with {} permissions", teacherRole.getPermissions().size());
    }
    
    private void initializeAdminRole() {
        Role adminRole = null;
        if (roleRepository.existsByName("ADMIN")) {
            // Update existing ADMIN role with new permissions
            adminRole = roleRepository.findByName("ADMIN").get();
            adminRole.getPermissions().clear(); // Clear old permissions
        } else {
            // Create new ADMIN role
            adminRole = Role.builder()
                    .name("ADMIN")
                    .permissions(new HashSet<>())
                    .build();
        }

        // Admin gets all permissions
        List<Permission> allPermissions = permissionRepository.findAll();
        adminRole.getPermissions().addAll(allPermissions);

        roleRepository.save(adminRole);
        log.info("Created/Updated ADMIN role with {} permissions", adminRole.getPermissions().size());
    }
}
