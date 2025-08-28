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

@DisplayName("RegisterRequest Validation Tests")
class RegisterRequestValidationTest {

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
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when name is null")
    void should_fail_validation_when_name_is_null() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name(null)
                .email("john@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Name is required");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("Should fail validation when name is blank")
    void should_fail_validation_when_name_is_blank() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("")
                .email("john@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        // Blank string triggers both @NotBlank and @Size constraints
        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Name is required", 
                        "Name must be between 2 and 100 characters"
                );
    }

    @Test
    @DisplayName("Should fail validation when name is too short")
    void should_fail_validation_when_name_is_too_short() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("A") // Only 1 character
                .email("john@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Name must be between 2 and 100 characters");
    }

    @Test
    @DisplayName("Should fail validation when name is too long")
    void should_fail_validation_when_name_is_too_long() {
        // Given
        String longName = "A".repeat(101); // 101 characters
        RegisterRequest request = RegisterRequest.builder()
                .name(longName)
                .email("john@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Name must be between 2 and 100 characters");
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void should_fail_validation_when_email_is_blank() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email is required");
    }

    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void should_fail_validation_when_email_is_invalid_format() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("invalid-email")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email must be valid");
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void should_accept_valid_email_formats() {
        String[] validEmails = {
                "user@example.com",
                "test.email@domain.org",
                "user+tag@example.co.uk"
        };

        for (String email : validEmails) {
            RegisterRequest request = RegisterRequest.builder()
                    .name("John Doe")
                    .email(email)
                    .password("password123")
                    .build();

            Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("Should have multiple validation errors for invalid data")
    void should_have_multiple_validation_errors_for_invalid_data() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("A") // Too short name (only @Size violation, not @NotBlank)
                .email("invalid-email") // Invalid email format
                .password("123") // Too short password
                .build();

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("name", "email", "password");
    }
}
