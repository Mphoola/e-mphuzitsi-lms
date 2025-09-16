package com.mphoola.e_empuzitsi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should return 404 with structured response for ResourceNotFoundException")
    void testResourceNotFoundException() {
        // Given
        String errorMessage = "Subject not found with id: 1";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);
        when(request.getRequestURI()).thenReturn("/api/subjects/1");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceNotFoundException(exception, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.get("message"));
        assertNotNull(body.get("errors"));
        assertNull(body.get("data"));
        
        // Verify Laravel-style structure
        assertTrue(body.containsKey("message"));
        assertTrue(body.containsKey("errors"));
        assertTrue(body.containsKey("data"));
    }

    @Test
    @DisplayName("Should return 409 with structured response for ResourceConflictException")
    void testResourceConflictException() {
        // Given
        String errorMessage = "Subject already exists with name: Mathematics";
        ResourceConflictException exception = new ResourceConflictException(errorMessage);
        when(request.getRequestURI()).thenReturn("/api/subjects");

        // When
        ResponseEntity<Map<String, Object>> response = 
            globalExceptionHandler.handleResourceConflictException(exception, request);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); // Now expects HTTP 409
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.get("message"));
        assertNotNull(body.get("errors"));
        assertNull(body.get("data"));
    }
}