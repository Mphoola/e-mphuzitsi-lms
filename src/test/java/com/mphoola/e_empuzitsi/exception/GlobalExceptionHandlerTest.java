package com.mphoola.e_empuzitsi.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle HTTP method not supported exception")
    void should_handle_method_not_supported_exception() {
        // Given
        HttpRequestMethodNotSupportedException exception = 
            new HttpRequestMethodNotSupportedException("POST");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleMethodNotSupportedException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(405);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("POST");
        assertThat(body.getMessage()).contains("not supported");
        assertThat(body.getStatus()).isEqualTo(405);
        assertThat(body.getError()).isEqualTo("Method Not Allowed");
        assertThat(body.getPath()).isEqualTo("/api/users");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should handle media type not supported exception")
    void should_handle_media_type_not_supported_exception() {
        // Given
        HttpMediaTypeNotSupportedException exception = 
            new HttpMediaTypeNotSupportedException("Unsupported media type");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleMediaTypeNotSupportedException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(415);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("Content type not supported");
        assertThat(body.getStatus()).isEqualTo(415);
        assertThat(body.getError()).isEqualTo("Unsupported Media Type");
        assertThat(body.getPath()).isEqualTo("/api/users");
    }

    @Test
    @DisplayName("Should handle missing servlet request parameter exception")
    void should_handle_missing_parameter_exception() {
        // Given
        MissingServletRequestParameterException exception = 
            new MissingServletRequestParameterException("userId", "String");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleMissingParameterException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Missing required parameter: userId");
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getPath()).isEqualTo("/api/users");
    }

    @Test
    @DisplayName("Should handle method argument type mismatch exception")
    void should_handle_type_mismatch_exception() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("userId");
        when(exception.getMessage()).thenReturn("Type mismatch");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/abc");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleTypeMismatchException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Invalid parameter type for 'userId'");
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getPath()).isEqualTo("/api/users/abc");
    }

    @Test
    @DisplayName("Should handle method argument not valid exception")
    void should_handle_validation_errors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        FieldError emailError = new FieldError("registerRequest", "email", "Email is required");
        FieldError passwordError = new FieldError("registerRequest", "password", "Password must be at least 8 characters");
        errors.add(emailError);
        errors.add(passwordError);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);
        
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleValidationErrors(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Validation failed");
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getPath()).isEqualTo("/api/auth/register");
        assertThat(body.getValidationErrors()).hasSize(2);
        assertThat(body.getValidationErrors().get(0).getField()).isEqualTo("email");
        assertThat(body.getValidationErrors().get(0).getMessage()).isEqualTo("Email is required");
        assertThat(body.getValidationErrors().get(1).getField()).isEqualTo("password");
        assertThat(body.getValidationErrors().get(1).getMessage()).isEqualTo("Password must be at least 8 characters");
    }

    @Test
    @DisplayName("Should handle authentication exception")
    void should_handle_authentication_exception() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAuthenticationException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Authentication failed: Invalid credentials");
        assertThat(body.getStatus()).isEqualTo(401);
        assertThat(body.getError()).isEqualTo("Unauthorized");
        assertThat(body.getPath()).isEqualTo("/api/auth/login");
    }

    @Test
    @DisplayName("Should handle custom bad credentials exception")
    void should_handle_custom_bad_credentials_exception() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("User not found");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAuthenticationException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Authentication failed: User not found");
        assertThat(body.getStatus()).isEqualTo(401);
        assertThat(body.getError()).isEqualTo("Unauthorized");
        assertThat(body.getPath()).isEqualTo("/api/auth/login");
    }

    @Test
    @DisplayName("Should handle access denied exception")
    void should_handle_access_denied_exception() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access is denied");
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/admin/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAccessDeniedException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(403);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Access denied: Access is denied");
        assertThat(body.getStatus()).isEqualTo(403);
        assertThat(body.getError()).isEqualTo("Forbidden");
        assertThat(body.getPath()).isEqualTo("/api/admin/users");
    }

    @Test
    @DisplayName("Should handle resource not found exception")
    void should_handle_resource_not_found_exception() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("User with ID 123 not found");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/123");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleResourceNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("User with ID 123 not found");
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Not Found");
        assertThat(body.getPath()).isEqualTo("/api/users/123");
    }

    @Test
    @DisplayName("Should handle resource conflict exception")
    void should_handle_resource_conflict_exception() {
        // Given
        ResourceConflictException exception = new ResourceConflictException("Email already exists");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/register");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleResourceConflictException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("Email already exists");
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getError()).isEqualTo("Conflict");
        assertThat(body.getPath()).isEqualTo("/api/auth/register");
    }

    @Test
    @DisplayName("Should handle runtime exception")
    void should_handle_runtime_exception() {
        // Given
        RuntimeException exception = new RuntimeException("Database connection failed");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleRuntimeException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("An internal error occurred");
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getError()).isEqualTo("Internal Server Error");
        assertThat(body.getPath()).isEqualTo("/api/users");
    }

    @Test
    @DisplayName("Should handle generic exception")
    void should_handle_generic_exception() {
        // Given
        Exception exception = new Exception("Unexpected error");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getError()).isEqualTo("Internal Server Error");
        assertThat(body.getPath()).isEqualTo("/api/health");
    }

    @Test
    @DisplayName("Should handle validation errors with multiple fields")
    void should_handle_multiple_validation_errors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        errors.add(new FieldError("user", "name", "Name is required"));
        errors.add(new FieldError("user", "email", "Invalid email format"));
        errors.add(new FieldError("user", "password", "Password too short"));
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);
        
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleValidationErrors(exception, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getValidationErrors()).hasSize(3);
        
        List<ErrorResponse.ValidationError> validationErrors = body.getValidationErrors();
        assertThat(validationErrors.stream().map(ErrorResponse.ValidationError::getField))
            .contains("name", "email", "password");
        assertThat(validationErrors.stream().map(ErrorResponse.ValidationError::getMessage))
            .contains("Name is required", "Invalid email format", "Password too short");
    }

    @Test
    @DisplayName("Should create error response with timestamp")
    void should_create_error_response_with_timestamp() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Test resource not found");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleResourceNotFoundException(exception, request);

        // Then
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTimestamp()).isNotNull();
        assertThat(body.getTimestamp()).isBefore(java.time.LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Should handle error response fields correctly")
    void should_handle_error_response_fields_correctly() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Insufficient privileges");
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/admin/settings");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
            .handleAccessDeniedException(exception, request);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getMessage()).isEqualTo("Access denied: Insufficient privileges");
        assertThat(errorResponse.getStatus()).isEqualTo(403);
        assertThat(errorResponse.getError()).isEqualTo("Forbidden");
        assertThat(errorResponse.getPath()).isEqualTo("/api/admin/settings");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getValidationErrors()).isNull();
    }

    @Test
    @DisplayName("Should preserve HTTP status codes correctly")
    void should_preserve_http_status_codes_correctly() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        // Test different status codes
        ResponseEntity<ErrorResponse> notFoundResponse = globalExceptionHandler
            .handleResourceNotFoundException(new ResourceNotFoundException("Not found"), request);
        
        ResponseEntity<ErrorResponse> conflictResponse = globalExceptionHandler
            .handleResourceConflictException(new ResourceConflictException("Conflict"), request);
        
        ResponseEntity<ErrorResponse> authResponse = globalExceptionHandler
            .handleAuthenticationException(new BadCredentialsException("Unauthorized"), request);

        // Then
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(conflictResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(authResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
