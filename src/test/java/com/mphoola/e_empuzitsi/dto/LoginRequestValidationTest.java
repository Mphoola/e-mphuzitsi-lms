package com.mphoola.e_empuzitsi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginRequest Validation Tests")
class LoginRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void should_pass_validation_with_valid_data() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when email is null")
    void should_fail_validation_when_email_is_null() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email(null)
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email is required");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void should_fail_validation_when_email_is_blank() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email is required");
    }

    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void should_fail_validation_when_email_is_invalid_format() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("invalid-email")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email must be valid");
    }

    @Test
    @DisplayName("Should fail validation when password is null")
    void should_fail_validation_when_password_is_null() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password(null)
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Password is required");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("password");
    }

    @Test
    @DisplayName("Should fail validation when password is blank")
    void should_fail_validation_when_password_is_blank() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("")
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Password is required");
    }

    @Test
    @DisplayName("Should accept valid credentials")
    void should_accept_valid_credentials() {
        String[] validPasswords = {
                "password",
                "123456",
                "p@ssw0rd!",
                "very-long-password-with-special-chars-123!"
        };

        for (String password : validPasswords) {
            LoginRequest request = LoginRequest.builder()
                    .email("user@example.com")
                    .password(password)
                    .build();

            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Should have multiple validation errors for invalid data")
    void should_have_multiple_validation_errors_for_invalid_data() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("invalid-email") // Invalid email format
                .password("") // Blank password
                .build();

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("email", "password");
    }
}