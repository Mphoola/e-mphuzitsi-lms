package com.mphoola.e_empuzitsi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
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

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle HTTP method not supported errors (405)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, 
            HttpServletRequest request) {
        
        log.warn("Method not supported for path: {} - {} method not supported", 
                request.getRequestURI(), request.getMethod());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("HTTP method '" + request.getMethod() + "' is not supported for this endpoint")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    /**
     * Handle route/endpoint not found errors (404)
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.warn("Endpoint not found: {} {}", request.getMethod(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("The requested endpoint was not found")
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle unsupported media type errors (415)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex, 
            HttpServletRequest request) {
        
        log.warn("Media type not supported for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Content type not supported. Please use 'application/json' or 'application/x-www-form-urlencoded'")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error("Unsupported Media Type")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }
    
    /**
     * Handle malformed JSON or request body errors (400)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, 
            HttpServletRequest request) {
        
        log.warn("Malformed request body for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Invalid request body. Please check your JSON format")
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle missing request parameters (400)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, 
            HttpServletRequest request) {
        
        log.warn("Missing parameter for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Missing required parameter: " + ex.getParameterName())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle type mismatch errors (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, 
            HttpServletRequest request) {
        
        log.warn("Type mismatch for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Invalid parameter type for '" + ex.getName() + "'")
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        log.warn("Validation error for path: {}", request.getRequestURI());
        
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return new ErrorResponse.ValidationError(fieldName, errorMessage);
                })
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle authentication errors
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, 
            HttpServletRequest request) {
        
        log.warn("Authentication error for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Authentication failed: " + ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    /**
     * Handle access denied errors
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, 
            HttpServletRequest request) {
        
        log.warn("Access denied for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Access denied: " + ex.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    /**
     * Handle resource not found errors
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        log.warn("Resource not found for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle resource conflict errors
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflictException(
            ResourceConflictException ex, 
            HttpServletRequest request) {
        
        log.warn("Resource conflict for path: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Handle general runtime errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, 
            HttpServletRequest request) {
        
        log.error("Runtime error for path: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("An internal error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Unexpected error for path: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
