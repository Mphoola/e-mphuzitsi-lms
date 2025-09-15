package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.auth.AuthResponse;
import com.mphoola.e_empuzitsi.dto.auth.LoginRequest;
import com.mphoola.e_empuzitsi.dto.auth.RegisterRequest;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.entity.UserRole;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.repository.RoleRepository;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import com.mphoola.e_empuzitsi.repository.UserRoleRepository;
import com.mphoola.e_empuzitsi.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserRepository userRepository,
                      RoleRepository roleRepository,
                      UserRoleRepository userRoleRepository,
                      UserService userService,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Register a new user
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User already exists with email: " + request.getEmail());
        }
        
        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        
        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verificationToken(verificationToken)
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Assign default STUDENT role
        assignDefaultRole(savedUser);
        
        // Send verification email
        userService.sendEmailVerification(savedUser.getEmail());
        
        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        // Get user response with roles and permissions using UserService
        // Reload user with roles and permissions
        User userWithRoles = userRepository.findByEmailWithRolesAndPermissions(savedUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after registration"));
        UserResponse userResponse = userService.mapToUserResponse(userWithRoles);
        
        return AuthResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }
    
    /**
     * Authenticate user and generate JWT token
     */
    public AuthResponse login(LoginRequest request) {
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
            
            // Get user response with roles and permissions
            User user = userRepository.findByEmailWithRolesAndPermissions(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            UserResponse userResponse = userService.mapToUserResponse(user);
            
            return AuthResponse.builder()
                    .token(token)
                    .user(userResponse)
                    .build();
                    
        } catch (AuthenticationException e) {
            throw new com.mphoola.e_empuzitsi.exception.BadCredentialsException("Invalid email or password");
        }
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
    }
}
