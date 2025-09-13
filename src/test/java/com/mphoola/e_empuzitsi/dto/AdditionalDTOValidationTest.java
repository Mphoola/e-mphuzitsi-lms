package com.mphoola.e_empuzitsi.dto;

import com.mphoola.e_empuzitsi.dto.auth.ForgotPasswordRequest;
import com.mphoola.e_empuzitsi.dto.auth.ResetPasswordRequest;
import com.mphoola.e_empuzitsi.dto.auth.AuthResponse;
import com.mphoola.e_empuzitsi.dto.auth.LoginRequest;
import com.mphoola.e_empuzitsi.dto.auth.RegisterRequest;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.dto.common.MessageResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Additional DTO Validation Tests")
class AdditionalDTOValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ForgotPasswordRequest Tests
    @Test
    @DisplayName("ForgotPasswordRequest - Should pass validation with valid email")
    void forgot_password_should_pass_validation_with_valid_email() {
        // Given
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("user@example.com")
                .build();

        // When
        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("ForgotPasswordRequest - Should fail validation when email is blank")
    void forgot_password_should_fail_validation_when_email_is_blank() {
        // Given
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("")
                .build();

        // When
        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ForgotPasswordRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email is required");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    @DisplayName("ForgotPasswordRequest - Should fail validation when email format is invalid")
    void forgot_password_should_fail_validation_when_email_format_is_invalid() {
        // Given
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("invalid-email-format")
                .build();

        // When
        Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ForgotPasswordRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email must be valid");
    }

    // ResetPasswordRequest Tests
    @Test
    @DisplayName("ResetPasswordRequest - Should pass validation with valid data")
    void reset_password_should_pass_validation_with_valid_data() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-reset-token-123")
                .newPassword("newPassword123")
                .build();

        // When
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("ResetPasswordRequest - Should fail validation when token is blank")
    void reset_password_should_fail_validation_when_token_is_blank() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("")
                .newPassword("newPassword123")
                .build();

        // When
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResetPasswordRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Reset token is required");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("token");
    }

    @Test
    @DisplayName("ResetPasswordRequest - Should fail validation when new password is blank")
    void reset_password_should_fail_validation_when_new_password_is_blank() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-reset-token-123")
                .newPassword("")
                .build();

        // When
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2); // Both @NotBlank and @Size violations
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("New password is required", "Password must be at least 8 characters");
    }

    @Test
    @DisplayName("ResetPasswordRequest - Should fail validation when new password is too short")
    void reset_password_should_fail_validation_when_new_password_is_too_short() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-reset-token-123")
                .newPassword("1234567") // Only 7 characters
                .build();

        // When
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ResetPasswordRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Password must be at least 8 characters");
    }

    @Test
    @DisplayName("ResetPasswordRequest - Should pass validation with minimum password length")
    void reset_password_should_pass_validation_with_minimum_password_length() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-reset-token-123")
                .newPassword("12345678") // Exactly 8 characters
                .build();

        // When
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("ResetPasswordRequest - Should have multiple validation errors when both fields are invalid")
    void reset_password_should_have_multiple_validation_errors() {
        // Given
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("")
                .newPassword("short")
                .build();

        // When
        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(ConstraintViolation::getPropertyPath)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("token", "newPassword");
    }

    // AuthResponse Tests
    @Test
    @DisplayName("AuthResponse - Should create with builder pattern")
    void auth_response_should_create_with_builder_pattern() {
        // Given
        UserResponse user = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        // When
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-123")
                .user(user)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getType()).isEqualTo("Bearer"); // Default value
        assertThat(response.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("AuthResponse - Should create with constructor")
    void auth_response_should_create_with_constructor() {
        // Given
        UserResponse user = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        // When
        AuthResponse response = new AuthResponse("jwt-token-123", user);

        // Then
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getType()).isNull(); // Constructor doesn't set default value
        assertThat(response.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("AuthResponse - Should set default type to Bearer")
    void auth_response_should_set_default_type_to_bearer() {
        // When
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-123")
                .build();

        // Then
        assertThat(response.getType()).isEqualTo("Bearer");
    }

    // MessageResponse Tests
    @Test
    @DisplayName("MessageResponse - Should create with builder pattern")
    void message_response_should_create_with_builder_pattern() {
        // When
        MessageResponse response = MessageResponse.builder()
                .message("Operation successful")
                .build();

        // Then
        assertThat(response.getMessage()).isEqualTo("Operation successful");
    }

    @Test
    @DisplayName("MessageResponse - Should create with constructor")
    void message_response_should_create_with_constructor() {
        // When
        MessageResponse response = new MessageResponse("Operation successful");

        // Then
        assertThat(response.getMessage()).isEqualTo("Operation successful");
    }

    @Test
    @DisplayName("MessageResponse - Should handle null message")
    void message_response_should_handle_null_message() {
        // When
        MessageResponse response = MessageResponse.builder()
                .message(null)
                .build();

        // Then
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("MessageResponse - Should handle empty message")
    void message_response_should_handle_empty_message() {
        // When
        MessageResponse response = MessageResponse.builder()
                .message("")
                .build();

        // Then
        assertThat(response.getMessage()).isEmpty();
    }

    // UserResponse Tests
    @Test
    @DisplayName("UserResponse - Should create with builder pattern")
    void user_response_should_create_with_builder_pattern() {
        // When
        UserResponse response = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("UserResponse - Should create with no-args constructor")
    void user_response_should_create_with_no_args_constructor() {
        // When
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setName("John Doe");
        response.setEmail("john@example.com");

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should test DTO equality and hashCode")
    void should_test_dto_equality_and_hash_code() {
        // Given
        LoginRequest request1 = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginRequest request2 = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LoginRequest request3 = LoginRequest.builder()
                .email("different@example.com")
                .password("password")
                .build();

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.toString()).contains("test@example.com");
    }

    @Test
    @DisplayName("Should test whitespace validation for all DTOs")
    void should_test_whitespace_validation_for_all_dtos() {
        // ForgotPasswordRequest with whitespace
        ForgotPasswordRequest forgotRequest = ForgotPasswordRequest.builder()
                .email("   ")
                .build();
        
        Set<ConstraintViolation<ForgotPasswordRequest>> forgotViolations = validator.validate(forgotRequest);
        assertThat(forgotViolations).hasSize(2); // Both @NotBlank and @Email violations
        assertThat(forgotViolations).extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Email is required", "Email must be valid");

        // ResetPasswordRequest with whitespace
        ResetPasswordRequest resetRequest = ResetPasswordRequest.builder()
                .token("   ")
                .newPassword("   ")
                .build();
        
        Set<ConstraintViolation<ResetPasswordRequest>> resetViolations = validator.validate(resetRequest);
        assertThat(resetViolations).hasSize(3); // token: @NotBlank, newPassword: @NotBlank + @Size
    }
}
