package com.mphoola.e_empuzitsi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        
        // Mock common request properties
        when(request.getRequestURI()).thenReturn("/api/subjects/1");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    @DisplayName("Should return HTTP 404 with structured response for ResourceNotFoundException")
    void handleResourceNotFoundException_ShouldReturnNotFoundResponse() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Subject not found with id: 1");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceNotFoundException(exception, request);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Subject not found with id: 1", body.get("message"));
        assertTrue(body.containsKey("errors"));
        assertTrue(body.containsKey("data"));
        assertNull(body.get("data"));
    }

    @Test
    @DisplayName("Should return HTTP 409 with structured response for ResourceConflictException")
    void handleResourceConflictException_ShouldReturnConflictResponse() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/subjects");
        when(request.getMethod()).thenReturn("POST");
        ResourceConflictException exception = new ResourceConflictException("Subject already exists with name: Mathematics");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceConflictException(exception, request);
        
        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Subject already exists with name: Mathematics", body.get("message"));
        assertTrue(body.containsKey("errors"));
        assertTrue(body.containsKey("data"));
        assertNull(body.get("data"));
    }

    @Test
    @DisplayName("Should return HTTP 403 with structured response for AccessDeniedException")
    void handleAccessDeniedException_ShouldReturnForbiddenResponse() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Insufficient privileges");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleAccessDeniedException(exception, request);
        
        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Access denied. You don't have sufficient permissions to access this resource.", body.get("message"));
        assertTrue(body.containsKey("errors"));
        assertTrue(body.containsKey("data"));
        assertNull(body.get("data"));
    }

    @Test
    @DisplayName("Should return HTTP 409 with structured response for RoleInUseException")
    void handleRoleInUseException_ShouldReturnConflictResponse() {
        // Given
        RoleInUseException exception = new RoleInUseException("Role is currently in use and cannot be deleted");
        
        // When
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRoleInUseException(exception, request);
        
        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Role is currently in use and cannot be deleted", body.get("message"));
        assertTrue(body.containsKey("errors"));
        assertTrue(body.containsKey("data"));
        assertNull(body.get("data"));
    }
}