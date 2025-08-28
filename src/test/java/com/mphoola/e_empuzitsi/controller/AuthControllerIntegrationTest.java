package com.mphoola.e_empuzitsi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mphoola.e_empuzitsi.dto.LoginRequest;
import com.mphoola.e_empuzitsi.dto.RegisterRequest;
import com.mphoola.e_empuzitsi.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Test
    void testRegisterUser_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("securePassword123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.name").value("John Doe"))
                .andExpect(jsonPath("$.user.email").value("john.doe@example.com"));
    }

    @Test
    void testLoginUser_Success() throws Exception {
        // Register user first
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("login.test@example.com")
                .password("LoginPass123!")
                .name("Login Test")
                .build();

        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("login.test@example.com")
                .password("LoginPass123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("login.test@example.com"));
    }

    @Test
    void testGetCurrentUser_WithValidToken() throws Exception {
        // Register and get token
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("current.user@example.com")
                .password("CurrentPass123!")
                .name("Current User")
                .build();

        String token = authService.register(registerRequest).getToken();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("current.user@example.com"))
                .andExpect(jsonPath("$.name").value("Current User"));
    }
}
