package com.mphoola.e_empuzitsi.config;

import com.mphoola.e_empuzitsi.security.CustomUserDetailsService;
import com.mphoola.e_empuzitsi.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("Should create SecurityConfig instance successfully")
    void should_create_security_config_instance() {
        // When
        SecurityConfig securityConfig = new SecurityConfig(customUserDetailsService, jwtAuthenticationFilter);
        
        // Then
        assertNotNull(securityConfig);
    }

    @Test
    @DisplayName("Should create PasswordEncoder bean as BCryptPasswordEncoder")
    void should_create_password_encoder_bean() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(customUserDetailsService, jwtAuthenticationFilter);
        
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        // Then
        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
        
        // Test encoding functionality
        String password = "testPassword";
        String encodedPassword = passwordEncoder.encode(password);
        
        assertThat(encodedPassword).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    @DisplayName("Should create CorsConfigurationSource bean with proper configuration")
    void should_create_cors_configuration_source_bean() {
        // Given
        SecurityConfig securityConfig = new SecurityConfig(customUserDetailsService, jwtAuthenticationFilter);
        
        // When
        CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();
        
        // Then
        assertNotNull(corsConfigurationSource);
        assertThat(corsConfigurationSource).isInstanceOf(org.springframework.web.cors.UrlBasedCorsConfigurationSource.class);
    }

    @Test
    @DisplayName("Should create beans with proper dependencies")
    void should_create_beans_with_proper_dependencies() {
        // Given
        SecurityConfig config = new SecurityConfig(customUserDetailsService, jwtAuthenticationFilter);
        
        // When
        PasswordEncoder encoder = config.passwordEncoder();
        CorsConfigurationSource corsSource = config.corsConfigurationSource();
        
        // Then
        assertNotNull(encoder);
        assertNotNull(corsSource);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    @DisplayName("Should handle CORS configuration properly")
    void should_handle_cors_configuration_properly() {
        // Given
        SecurityConfig config = new SecurityConfig(customUserDetailsService, jwtAuthenticationFilter);
        
        // When
        CorsConfigurationSource source = config.corsConfigurationSource();
        
        // Then
        assertThat(source).isNotNull();
        assertThat(source).isInstanceOf(org.springframework.web.cors.UrlBasedCorsConfigurationSource.class);
    }

    @Test
    @DisplayName("Should create BCryptPasswordEncoder with proper strength")
    void should_create_bcrypt_password_encoder_with_proper_strength() {
        // Given
        SecurityConfig config = new SecurityConfig(customUserDetailsService, jwtAuthenticationFilter);
        
        // When
        PasswordEncoder passwordEncoder = config.passwordEncoder();
        String password = "TestPassword123!";
        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);
        
        // Then - BCrypt should generate different hashes for same password
        assertThat(encoded1).isNotEqualTo(encoded2);
        assertThat(passwordEncoder.matches(password, encoded1)).isTrue();
        assertThat(passwordEncoder.matches(password, encoded2)).isTrue();
        
        // Test password strength requirements
        assertThat(encoded1.length()).isGreaterThan(50); // BCrypt hashes are typically 60 chars
    }
}
