package com.mphoola.e_empuzitsi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for HealthController
 * Tests all health check endpoints
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
public class HealthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Test
    public void should_return_simple_health_status() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // When & Then
        mockMvc.perform(get("/api/health/simple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Application is running!"));
    }

    @Test
    public void should_return_root_health_status() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // When & Then
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.message").value("Application is running!"))
                .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    public void should_return_detailed_health_check() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        
        // When & Then
        mockMvc.perform(get("/api/health/check"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.database").exists())
                .andExpect(jsonPath("$.data.timestamp").exists());
    }
}
