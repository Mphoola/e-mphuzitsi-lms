package com.mphoola.e_empuzitsi.util;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtUtil
 * Tests JWT token generation, validation, claims extraction, and edge cases
 */
@SpringBootTest
@ActiveProfiles("test")
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    
    private final String testSecret = "myTestSecretKeyThatMustBeAtLeast256BitsLongForHMACAlgorithmToWorkProperly";
    private final int testExpiration = 3600; // 1 hour in seconds
    private final String testUsername = "test@example.com";
    private final List<GrantedAuthority> testAuthorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
    );

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
        // Set test values using reflection to avoid Spring context dependency
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationInMs", testExpiration);
    }

    @Test
    public void should_generate_token_from_authentication() {
        // Given
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = User.builder()
                .username(testUsername)
                .password("password")
                .authorities(testAuthorities)
                .build();
        
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // When
        String token = jwtUtil.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
        
        // Verify the token contains expected data
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        String extractedAuthorities = jwtUtil.getAuthoritiesFromToken(token);
        
        assertThat(extractedUsername).isEqualTo(testUsername);
        // The order of authorities might vary, so we just check both are present
        assertThat(extractedAuthorities).contains("ROLE_USER").contains("ROLE_ADMIN");
    }

    @Test
    public void should_generate_token_from_username() {
        // When
        String token = jwtUtil.generateToken(testUsername);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        
        // Verify the token contains expected username
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertThat(extractedUsername).isEqualTo(testUsername);
        
        // Token generated from username should not have authorities
        String extractedAuthorities = jwtUtil.getAuthoritiesFromToken(token);
        assertThat(extractedAuthorities).isNull();
    }

    @Test
    public void should_extract_username_from_valid_token() {
        // Given
        String token = jwtUtil.generateToken(testUsername);

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(testUsername);
    }

    @Test
    public void should_extract_authorities_from_token_with_authentication() {
        // Given
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = User.builder()
                .username(testUsername)
                .password("password")
                .authorities(testAuthorities)
                .build();
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        String token = jwtUtil.generateToken(authentication);

        // When
        String extractedAuthorities = jwtUtil.getAuthoritiesFromToken(token);

        // Then
        // The order of authorities might vary, so we just check both are present
        assertThat(extractedAuthorities).contains("ROLE_USER").contains("ROLE_ADMIN");
    }

    @Test
    public void should_return_null_authorities_from_username_token() {
        // Given
        String token = jwtUtil.generateToken(testUsername);

        // When
        String extractedAuthorities = jwtUtil.getAuthoritiesFromToken(token);

        // Then
        assertThat(extractedAuthorities).isNull();
    }

    @Test
    public void should_extract_expiration_date_from_token() {
        // Given
        String token = jwtUtil.generateToken(testUsername);

        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        // Then
        assertThat(expirationDate).isNotNull();
        
        // Token should expire in the future (more than current time)
        long currentTime = System.currentTimeMillis();
        assertThat(expirationDate.getTime()).isGreaterThan(currentTime);
        
        // Token should expire approximately within 1 hour from now (allowing some tolerance)
        long oneHourFromNow = currentTime + (testExpiration * 1000L);
        long tolerance = 5000L; // 5 seconds tolerance
        assertThat(expirationDate.getTime())
                .isBetween(currentTime + (testExpiration * 1000L) - tolerance, 
                          oneHourFromNow + tolerance);
    }

    @Test
    public void should_detect_non_expired_token() {
        // Given
        String token = jwtUtil.generateToken(testUsername);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    public void should_detect_expired_token() throws InterruptedException {
        // Given - create JwtUtil with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "jwtExpirationInMs", -1000); // Expired already
        
        String expiredToken = shortExpirationJwtUtil.generateToken(testUsername);

        // When & Then - the token should not be valid (which means it's expired)
        // This should return false because the token is expired
        try {
            boolean isExpired = shortExpirationJwtUtil.isTokenExpired(expiredToken);
            assertThat(isExpired).isTrue();
        } catch (ExpiredJwtException ex) {
            // Expected behavior - token is expired
            assertThat(ex.getMessage()).contains("JWT expired");
        }
        
        boolean isValid = shortExpirationJwtUtil.validateToken(expiredToken);
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_validate_correct_token() {
        // Given
        String token = jwtUtil.generateToken(testUsername);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    public void should_reject_malformed_token() {
        // Given
        String malformedToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtUtil.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_reject_token_with_invalid_signature() {
        // Given
        String validToken = jwtUtil.generateToken(testUsername);
        // Tamper with the signature by changing the last character
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";

        // When
        boolean isValid = jwtUtil.validateToken(tamperedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_reject_expired_token_during_validation() {
        // Given - create JwtUtil with very short expiration
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "jwtExpirationInMs", -1); // Expired immediately
        
        String expiredToken = shortExpirationJwtUtil.generateToken(testUsername);

        // When
        boolean isValid = jwtUtil.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_reject_empty_token() {
        // When
        boolean isValid = jwtUtil.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_reject_null_token() {
        // When
        boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_handle_token_with_different_secret() {
        // Given
        JwtUtil differentSecretJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentSecretJwtUtil, "jwtSecret", "differentSecretKey256BitsLongForHMACAlgorithmToWorkProperly");
        ReflectionTestUtils.setField(differentSecretJwtUtil, "jwtExpirationInMs", testExpiration);
        
        String tokenWithDifferentSecret = differentSecretJwtUtil.generateToken(testUsername);

        // When - try to validate with original secret
        boolean isValid = jwtUtil.validateToken(tokenWithDifferentSecret);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    public void should_generate_different_tokens_for_same_user() throws InterruptedException {
        // Given - small delay to ensure different issuedAt timestamps
        String token1 = jwtUtil.generateToken(testUsername);
        Thread.sleep(1000); // Ensure different timestamps (1 second)
        String token2 = jwtUtil.generateToken(testUsername);

        // When & Then
        assertThat(token1).isNotEqualTo(token2); // Different issued timestamps should create different tokens
        
        // But both should be valid and contain the same username
        assertThat(jwtUtil.validateToken(token1)).isTrue();
        assertThat(jwtUtil.validateToken(token2)).isTrue();
        assertThat(jwtUtil.getUsernameFromToken(token1)).isEqualTo(testUsername);
        assertThat(jwtUtil.getUsernameFromToken(token2)).isEqualTo(testUsername);
    }

    @Test
    public void should_handle_token_with_no_authorities_claim() {
        // Given
        String token = jwtUtil.generateToken(testUsername); // This token won't have authorities

        // When
        String authorities = jwtUtil.getAuthoritiesFromToken(token);

        // Then
        assertThat(authorities).isNull();
    }

    @Test
    public void should_handle_single_authority() {
        // Given
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = User.builder()
                .username(testUsername)
                .password("password")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        String token = jwtUtil.generateToken(authentication);

        // When
        String extractedAuthorities = jwtUtil.getAuthoritiesFromToken(token);

        // Then
        assertThat(extractedAuthorities).isEqualTo("ROLE_USER");
    }
}
