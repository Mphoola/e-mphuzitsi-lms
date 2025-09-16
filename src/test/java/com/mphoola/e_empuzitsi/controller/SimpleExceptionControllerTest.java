package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.exception.GlobalExceptionHandler;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple test to verify structured exception responses without complex dependencies
 */
@DisplayName("Exception Handler Response Tests")
class SimpleExceptionControllerTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test")
    static class TestController {
        
        @GetMapping("/not-found")
        public String throwNotFoundException() {
            throw new ResourceNotFoundException("Test resource not found");
        }
        
        @GetMapping("/conflict")
        public String throwConflictException() {
            throw new ResourceConflictException("Test resource conflict");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return HTTP 404 with structured response for ResourceNotFoundException")
    void shouldReturnNotFoundWithStructuredResponse() throws Exception {
        mockMvc.perform(get("/test/not-found")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test resource not found"))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should return HTTP 409 with structured response for ResourceConflictException")
    void shouldReturnConflictWithStructuredResponse() throws Exception {
        mockMvc.perform(get("/test/conflict")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test resource conflict"))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}