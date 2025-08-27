package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.AuthResponse;
import com.mphoola.e_empuzitsi.dto.LoginRequest;
import com.mphoola.e_empuzitsi.dto.RegisterRequest;
import com.mphoola.e_empuzitsi.dto.UserResponse;
import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.entity.UserRole;
import com.mphoola.e_empuzitsi.entity.UserPermission;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import com.mphoola.e_empuzitsi.repository.UserRoleRepository;
import com.mphoola.e_empuzitsi.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserRepository userRepository,
                      RoleRepository roleRepository,
                      UserRoleRepository userRoleRepository,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User already exists with email: " + request.getEmail());
        }
        
        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("Successfully created user with ID: {}", savedUser.getId());
        
        // Assign default STUDENT role
        assignDefaultRole(savedUser);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        // Get user response with roles and permissions
        UserResponse userResponse = mapToUserResponse(savedUser);
        
        log.info("Successfully registered and authenticated user: {}", savedUser.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }
    
    /**
     * Authenticate user and generate JWT token
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            // Generate JWT token
            String token = jwtUtil.generateToken(authentication);
            
            // Get user response
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            UserResponse userResponse = mapToUserResponse(user);
            
            log.info("Successfully authenticated user: {}", request.getEmail());
            
            return AuthResponse.builder()
                    .token(token)
                    .user(userResponse)
                    .build();
                    
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {}", request.getEmail(), e.getMessage());
            throw new com.mphoola.e_empuzitsi.exception.BadCredentialsException("Invalid email or password");
        }
    }
    
    /**
     * Get current authenticated user details
     */
    public UserResponse getCurrentUser() {
        // We'll implement this in the controller to avoid circular dependency
        throw new UnsupportedOperationException("Use UserService.getCurrentUser() instead");
    }
    
    /**
     * Assign default STUDENT role to newly registered user
     */
    private void assignDefaultRole(User user) {
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new ResourceNotFoundException("STUDENT role not found. Please ensure roles are initialized."));
        
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(studentRole)
                .build();
        
        userRoleRepository.save(userRole);
        log.debug("Assigned STUDENT role to user: {}", user.getEmail());
    }
    
    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        // For now, return minimal user response without roles/permissions to avoid lazy loading issues
        Set<String> roles = new HashSet<>();
        Set<String> allPermissions = new HashSet<>();
        
        try {
            // Try to extract roles safely
            if (user.getUserRoles() != null && !user.getUserRoles().isEmpty()) {
                for (UserRole userRole : user.getUserRoles()) {
                    if (userRole != null && userRole.getRole() != null) {
                        roles.add(userRole.getRole().getName());
                        
                        // Try to extract role permissions safely
                        if (userRole.getRole().getPermissions() != null) {
                            for (Permission permission : userRole.getRole().getPermissions()) {
                                if (permission != null) {
                                    allPermissions.add(permission.getName());
                                }
                            }
                        }
                    }
                }
            }
            
            // Try to extract user permissions safely
            if (user.getUserPermissions() != null && !user.getUserPermissions().isEmpty()) {
                for (UserPermission userPermission : user.getUserPermissions()) {
                    if (userPermission != null && userPermission.getPermission() != null) {
                        allPermissions.add(userPermission.getPermission().getName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error loading roles/permissions for user {}: {}", user.getEmail(), e.getMessage());
            // Return empty collections if there's an issue
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roles)
                .permissions(allPermissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
