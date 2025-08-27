package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.UserResponse;
import com.mphoola.e_empuzitsi.entity.*;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get user by ID with roles and permissions
     */
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return mapToUserResponse(user);
    }
    
    /**
     * Get user by email with roles and permissions
     */
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
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
        log.debug("Getting current user with email: {}", email);
        
        return getUserByEmail(email);
    }
    
    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
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
