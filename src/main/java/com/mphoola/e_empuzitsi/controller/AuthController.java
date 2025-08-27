package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.AuthResponse;
import com.mphoola.e_empuzitsi.dto.LoginRequest;
import com.mphoola.e_empuzitsi.dto.RegisterRequest;
import com.mphoola.e_empuzitsi.dto.UserResponse;
import com.mphoola.e_empuzitsi.service.AuthService;
import com.mphoola.e_empuzitsi.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    private final UserService userService;
    
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    
    /**
     * Register a new user (JSON)
     * POST /api/auth/register
     */
    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration JSON request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Register a new user (Form Data)
     * POST /api/auth/register
     */
    @PostMapping(value = "/register", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<AuthResponse> registerForm(@Valid @ModelAttribute RegisterRequest request) {
        log.info("Registration form request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * User login (JSON)
     * POST /api/auth/login
     */
    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login JSON request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * User login (Form Data)
     * POST /api/auth/login
     */
    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<AuthResponse> loginForm(@Valid @ModelAttribute LoginRequest request) {
        log.info("Login form request received for email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user details
     * GET /api/auth/me
     * Requires authentication
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.debug("Current user details requested");
        
        UserResponse user = userService.getCurrentUser();
        
        return ResponseEntity.ok(user);
    }
}
