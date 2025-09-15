package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.user.UpdateUserRequest;
import com.mphoola.e_empuzitsi.dto.user.UserRequest;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.dto.user.UserResponseSimple;
import com.mphoola.e_empuzitsi.entity.*;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.BadCredentialsException;
import com.mphoola.e_empuzitsi.mail.notifications.EmailVerificationEmail;
import com.mphoola.e_empuzitsi.mail.notifications.UserCredentialsEmail;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import com.mphoola.e_empuzitsi.util.PasswordGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, PasswordGenerator passwordGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.passwordGenerator = passwordGenerator;
    }
    
    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Create a new user with auto-generated password
     */
    public UserResponse createUser(UserRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User already exists with email: " + request.getEmail());
        }
        
        // Generate secure temporary password
        String temporaryPassword = passwordGenerator.generatePassword();
        
        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .accountType(request.getAccountType() != null ? request.getAccountType() : AccountType.STUDENT)
                .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Send credentials via email
        try {
            String frontendUrl = "http://localhost:3000/auth/login"; // TODO: Move to configuration
            UserCredentialsEmail credentialsEmail = new UserCredentialsEmail(
                savedUser.getEmail(),
                savedUser.getName(),
                temporaryPassword,
                frontendUrl,
                savedUser.getAccountType().toString()
            );
            emailService.sendEmail(credentialsEmail);
        } catch (Exception e) {
            // Log error but don't fail user creation
            System.err.println("Failed to send credentials email: " + e.getMessage());
        }
        
        // Return user response with roles and permissions
        User userWithRoles = userRepository.findByIdWithRolesAndPermissions(savedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after creation"));
        
        return mapToUserResponse(userWithRoles);
    }
    
    /**
     * Update an existing user
     */
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Update user fields
        user.setName(request.getName());
        
        // Update account type if provided
        if (request.getAccountType() != null) {
            user.setAccountType(request.getAccountType());
        }
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Return user response with roles and permissions
        User userWithRoles = userRepository.findByIdWithRolesAndPermissions(savedUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after update"));
        
        return mapToUserResponse(userWithRoles);
    }
    
    /**
     * Ban a user (soft delete)
     */
    public void banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
    }
    
    /**
     * Unban/activate a user
     */
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
    
    /**
     * Check if user is active (not banned)
     */
    public boolean isUserActive(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null && user.getStatus() == UserStatus.ACTIVE;
    }
    
    /**
     * Get all users (simple response) - Legacy method for backward compatibility
     */
    public List<UserResponseSimple> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream()
                .map(this::mapToUserResponseSimple)
                .toList();
    }

    /**
     * Get all users with pagination, filtering, sorting, and searching
     */
    public Page<UserResponseSimple> getAllUsers(String search, AccountType accountType, UserStatus status, Pageable pageable) {
        Page<User> users = userRepository.findUsersWithFilters(search, accountType, status, pageable);
        
        return users.map(this::mapToUserResponseSimple);
    }
    
    /**
     * Get user by ID with roles and permissions
     */
    public UserResponse getUserById(Long id) {
        
        User user = userRepository.findByIdWithRolesAndPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return mapToUserResponse(user);
    }
    
    /**
     * Get user by email with roles and permissions
     */
    public UserResponse getUserByEmail(String email) {        
        User user = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return mapToUserResponse(user);
    }
    
    /**
     * Get current authenticated user
     */
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }
        
        String email = authentication.getName();
        
        return getUserByEmail(email);
    }
    
    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Generate and send password reset token
     */
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        
        // Set token and expiry (1 hours from now)
        user.setResetToken(resetToken);
        user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
        
        userRepository.save(user);
        
        // Send email with reset token
        emailService.sendPasswordResetEmail(email, resetToken);
    }
    
    /**
     * Reset password using token
     */
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));
        
        // Check if token is expired
        if (user.getResetTokenExpiresAt() == null || user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Reset token has expired");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Clear reset token
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        
        userRepository.save(user);
        
        // Send confirmation email asynchronously
        emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getName());
    }
    
    /**
     * Map User entity to UserResponse DTO
     * Public method to be used by other services
     */
    public UserResponse mapToUserResponse(User user) {
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
            // Return empty collections if there's an issue
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .accountType(user.getAccountType())
                .status(user.getStatus())
                .roles(roles)
                .permissions(allPermissions)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    /**
     * Map User entity to UserResponseSimple DTO
     */
    private UserResponseSimple mapToUserResponseSimple(User user) {
        return UserResponseSimple.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .accountType(user.getAccountType().toString())
                .status(user.getStatus().toString())
                .build();
    }
    
    /**
     * Generate and send email verification token
     */
    public void sendEmailVerification(String email) {
        User user = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        
        // Set verification token
        user.setVerificationToken(verificationToken);
        userRepository.save(user);
        
        // Send verification email
        String verificationUrl = "http://localhost:3000/auth/verify-email?token=" + verificationToken;
        EmailVerificationEmail emailTemplate = new EmailVerificationEmail(
            email, 
            user.getName(), 
            verificationToken, 
            verificationUrl
        );
        emailService.sendEmail(emailTemplate);
    }
    
    /**
     * Verify email using token
     */
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid verification token"));
        
        // Mark email as verified
        user.markEmailAsVerified();
        userRepository.save(user);
    }
    
    /**
     * Check if user's email is verified
     */
    public boolean isEmailVerified(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null && user.isEmailVerified();
    }
    
    /**
     * Resend verification email
     */
    public void resendEmailVerification(String email) {
        User user = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        if (user.isEmailVerified()) {
            throw new BadCredentialsException("Email is already verified");
        }
        
        sendEmailVerification(email);
    }
}
