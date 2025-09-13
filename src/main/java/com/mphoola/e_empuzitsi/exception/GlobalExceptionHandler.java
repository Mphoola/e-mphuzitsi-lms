package com.mphoola.e_empuzitsi.exception;

import com.mphoola.e_empuzitsi.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle HTTP method not supported errors (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, 
            HttpServletRequest request) {
        
        log.warn("Method not supported for path: {} - {} method not supported", 
                request.getRequestURI(), request.getMethod());
        
        String message = "HTTP method '" + request.getMethod() + "' is not supported for this endpoint";
        return ApiResponse.failed(message);
    }
    
    /**
     * Handle route/endpoint not found errors (404)
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.warn("Endpoint not found: {} {}", request.getMethod(), request.getRequestURI());
        
        return ApiResponse.failed("The requested endpoint was not found");
    }
    
    /**
     * Handle unsupported media type errors (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, 
            HttpServletRequest request) {
        
        log.warn("Media type not supported for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        String message = "Content type not supported. Please use 'application/json' or 'application/x-www-form-urlencoded'";
        return ApiResponse.unprocessable(message);
    }
    
    /**
     * Handle malformed JSON or request body errors (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, 
            HttpServletRequest request) {
        
        log.warn("Malformed request body for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.unprocessable("Invalid request body. Please check your JSON format");
    }
    
    /**
     * Handle missing request parameters (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameterException(
            MissingServletRequestParameterException ex, 
            HttpServletRequest request) {
        
        log.warn("Missing parameter for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        String message = "Missing required parameter: " + ex.getParameterName();
        return ApiResponse.unprocessable(message);
    }
    
    /**
     * Handle type mismatch errors (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, 
            HttpServletRequest request) {
        
        log.warn("Type mismatch for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        String message = "Invalid parameter type for '" + ex.getName() + "'";
        return ApiResponse.unprocessable(message);
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        log.warn("Validation error for path: {}", request.getRequestURI());
        
        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    validationErrors.put(fieldName, errorMessage);
                });
        
        return ApiResponse.unprocessable("Validation failed", validationErrors);
    }
    
    /**
     * Handle authentication errors
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, 
            HttpServletRequest request) {
        
        return ApiResponse.unauthorized("Authentication failed. Please provide valid credentials.");
    }
    
    /**
     * Handle bad credentials errors (401 - Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, 
            HttpServletRequest request) {
        
        return ApiResponse.unauthorized("Invalid username or password.");
    }
    
    /**
     * Handle insufficient authentication errors (401 - Unauthorized)
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, 
            HttpServletRequest request) {
        
        return ApiResponse.unauthorized("Authentication required. Please login to access this resource.");
    }
    
    /**
     * Handle JWT expired token errors (401 - Unauthorized)
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwtException(
            ExpiredJwtException ex, 
            HttpServletRequest request) {
        
        return ApiResponse.unauthorized("Token has expired. Please login again.");
    }
    
    /**
     * Handle JWT signature errors (401 - Unauthorized)
     */
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleSignatureException(
            SignatureException ex, 
            HttpServletRequest request) {
        
        log.warn("Invalid JWT signature for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.unauthorized("Invalid token signature. Please login again.");
    }
    
    /**
     * Handle malformed JWT token errors (401 - Unauthorized)
     */
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJwtException(
            MalformedJwtException ex, 
            HttpServletRequest request) {
        
        log.warn("Malformed JWT token for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.unauthorized("Invalid token format. Please login again.");
    }
    
    /**
     * Handle unsupported JWT token errors (401 - Unauthorized)
     */
    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedJwtException(
            UnsupportedJwtException ex, 
            HttpServletRequest request) {
        
        log.warn("Unsupported JWT token for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.unauthorized("Unsupported token format. Please login again.");
    }
    
    /**
     * Handle general JWT errors (401 - Unauthorized)
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(
            JwtException ex, 
            HttpServletRequest request) {
        
        log.warn("JWT error for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.unauthorized("Token validation failed. Please login again.");
    }
    
    /**
     * Handle access denied errors (403 - Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, 
            HttpServletRequest request) {
        
        log.warn("Access denied for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.forbidden("Access denied. You don't have sufficient permissions to access this resource.");
    }
    
    /**
     * Handle resource not found errors
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        log.warn("Resource not found for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.failed(ex.getMessage());
    }
    
    /**
     * Handle resource conflict errors
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, Object>> handleResourceConflictException(
            ResourceConflictException ex, 
            HttpServletRequest request) {
        
        log.warn("Resource conflict for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.failed(ex.getMessage());
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex, 
            HttpServletRequest request) {
        
        log.warn("Validation error for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.unprocessable(ex.getMessage());
    }

    /**
     * Handle role in use errors
     */
    @ExceptionHandler(RoleInUseException.class)
    public ResponseEntity<Map<String, Object>> handleRoleInUseException(
            RoleInUseException ex, 
            HttpServletRequest request) {
        
        log.warn("Role in use for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        return ApiResponse.failed(ex.getMessage());
    }
    
    /**
     * Handle general runtime errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, 
            HttpServletRequest request) {
        
        log.error("Runtime error for path: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ApiResponse.failed("An internal error occurred");
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Unexpected error for path: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ApiResponse.failed("An unexpected error occurred");
    }
}
