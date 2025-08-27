package com.mphoola.e_empuzitsi.security;

import com.mphoola.e_empuzitsi.entity.Permission;
import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.entity.UserPermission;
import com.mphoola.e_empuzitsi.entity.UserRole;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        try {
            // Temporarily use simple query to debug
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
            
            log.debug("Found user: {} with ID: {}", user.getEmail(), user.getId());
            return UserPrincipal.create(user, getAuthorities(user));
            
        } catch (Exception e) {
            log.error("Error loading user by email {}: {}", email, e.getMessage(), e);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }
    
    /**
     * Load user by ID for JWT authentication
     */
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);
        
        // Temporarily use simple query to debug
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        return UserPrincipal.create(user, getAuthorities(user));
    }
    
    /**
     * Get all authorities (roles and permissions) for a user
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        try {
            // Add role-based authorities
            if (user.getUserRoles() != null) {
                for (UserRole userRole : user.getUserRoles()) {
                    Role role = userRole.getRole();
                    if (role != null) {
                        // Add role as authority
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                        
                        // Add role permissions
                        if (role.getPermissions() != null) {
                            for (Permission permission : role.getPermissions()) {
                                if (permission != null) {
                                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                                }
                            }
                        }
                    }
                }
            }
            
            // Add user-specific permissions (overrides)
            if (user.getUserPermissions() != null) {
                for (UserPermission userPermission : user.getUserPermissions()) {
                    Permission permission = userPermission.getPermission();
                    if (permission != null) {
                        authorities.add(new SimpleGrantedAuthority(permission.getName()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error loading authorities for user {}: {}. Using empty authorities.", 
                     user.getEmail(), e.getMessage());
            // Return basic user authority if role/permission loading fails
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        log.debug("User {} has authorities: {}", user.getEmail(), authorities);
        return authorities;
    }
}
