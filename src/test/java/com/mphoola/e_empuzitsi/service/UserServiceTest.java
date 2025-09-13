package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.exception.BadCredentialsException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService
 * Tests user retrieval, password reset, and user mapping functionality
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Authentication mockAuthentication;
    private SecurityContext mockSecurityContext;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockAuthentication = mock(Authentication.class);
        mockSecurityContext = mock(SecurityContext.class);
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUserResponse() {
        // Given
        when(userRepository.findByIdWithRolesAndPermissions(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getName()).isEqualTo(testUser.getName());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).findByIdWithRolesAndPermissions(1L);
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findByIdWithRolesAndPermissions(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findByIdWithRolesAndPermissions(999L);
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUserResponse() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmailWithRolesAndPermissions(email)).thenReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.getUserByEmail(email);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getName()).isEqualTo(testUser.getName());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());

        verify(userRepository).findByEmailWithRolesAndPermissions(email);
    }

    @Test
    void getUserByEmail_WithInvalidEmail_ShouldThrowResourceNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmailWithRolesAndPermissions(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with email: " + email);

        verify(userRepository).findByEmailWithRolesAndPermissions(email);
    }

    @Test
    void getCurrentUser_WithAuthenticatedUser_ShouldReturnUserResponse() {
        // Given
        String email = "john.doe@example.com";
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
            when(mockAuthentication.isAuthenticated()).thenReturn(true);
            when(mockAuthentication.getName()).thenReturn(email);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);
            
            when(userRepository.findByEmailWithRolesAndPermissions(email)).thenReturn(Optional.of(testUser));

            // When
            UserResponse result = userService.getCurrentUser();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);

            verify(userRepository).findByEmailWithRolesAndPermissions(email);
        }
    }

    @Test
    void getCurrentUser_WithNoAuthentication_ShouldThrowResourceNotFoundException() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(mockSecurityContext.getAuthentication()).thenReturn(null);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            // When/Then
            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No authenticated user found");
        }
    }

    @Test
    void getCurrentUser_WithUnauthenticatedUser_ShouldThrowResourceNotFoundException() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
            when(mockAuthentication.isAuthenticated()).thenReturn(false);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            // When/Then
            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No authenticated user found");
        }
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void forgotPassword_WithValidEmail_ShouldGenerateTokenAndSendEmail() {
        // Given
        String email = "john.doe@example.com";
        when(userRepository.findByEmailWithRolesAndPermissions(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());

        // When
        userService.forgotPassword(email);

        // Then
        verify(userRepository).findByEmailWithRolesAndPermissions(email);
        verify(userRepository).save(argThat(user -> {
            assertThat(user.getResetToken()).isNotNull();
            assertThat(user.getResetTokenExpiresAt()).isAfter(LocalDateTime.now().plusHours(23));
            return true;
        }));
        verify(emailService).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    void forgotPassword_WithInvalidEmail_ShouldThrowResourceNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmailWithRolesAndPermissions(email)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.forgotPassword(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with email: " + email);

        verify(userRepository).findByEmailWithRolesAndPermissions(email);
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_WithValidToken_ShouldUpdatePassword() {
        // Given
        String token = UUID.randomUUID().toString();
        String newPassword = "newPassword123";
        String encodedPassword = "encodedNewPassword";
        
        testUser.setResetToken(token);
        testUser.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.resetPassword(token, newPassword);

        // Then
        verify(userRepository).findByResetToken(token);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(argThat(user -> {
            assertThat(user.getPassword()).isEqualTo(encodedPassword);
            assertThat(user.getResetToken()).isNull();
            assertThat(user.getResetTokenExpiresAt()).isNull();
            return true;
        }));
    }

    @Test
    void resetPassword_WithInvalidToken_ShouldThrowBadCredentialsException() {
        // Given
        String token = "invalid-token";
        String newPassword = "newPassword123";
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.resetPassword(token, newPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid or expired reset token");

        verify(userRepository).findByResetToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_WithExpiredToken_ShouldThrowBadCredentialsException() {
        // Given
        String token = UUID.randomUUID().toString();
        String newPassword = "newPassword123";
        
        testUser.setResetToken(token);
        testUser.setResetTokenExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> userService.resetPassword(token, newPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Reset token has expired");

        verify(userRepository).findByResetToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetPassword_WithNullExpiryDate_ShouldThrowBadCredentialsException() {
        // Given
        String token = UUID.randomUUID().toString();
        String newPassword = "newPassword123";
        
        testUser.setResetToken(token);
        testUser.setResetTokenExpiresAt(null); // Null expiry date
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> userService.resetPassword(token, newPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Reset token has expired");

        verify(userRepository).findByResetToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void mapToUserResponse_WithValidUser_ShouldReturnUserResponse() {
        // Given - User without roles/permissions to test basic mapping

        // When
        UserResponse result = userService.mapToUserResponse(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getName()).isEqualTo(testUser.getName());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(testUser.getUpdatedAt());
        assertThat(result.getRoles()).isNotNull();
        assertThat(result.getPermissions()).isNotNull();
    }
}
