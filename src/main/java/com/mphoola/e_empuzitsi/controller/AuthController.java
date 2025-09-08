package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.AuthResponse;
import com.mphoola.e_empuzitsi.dto.LoginRequest;
import com.mphoola.e_empuzitsi.dto.RegisterRequest;
import com.mphoola.e_empuzitsi.dto.UserResponse;
import com.mphoola.e_empuzitsi.dto.ForgotPasswordRequest;
import com.mphoola.e_empuzitsi.dto.ResetPasswordRequest;
import com.mphoola.e_empuzitsi.dto.MessageResponse;
import com.mphoola.e_empuzitsi.service.AuthService;
import com.mphoola.e_empuzitsi.service.UserService;
import com.mphoola.e_empuzitsi.util.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth", produces = "application/json")
@Tag(name = "🔐 Authentication", description = "Authentication and user management endpoints")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    
    @Operation(summary = "Register New User")
    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {        
        AuthResponse response = authService.register(request);
        return ApiResponse.success("User registered successfully", response);
    }
    
    @Operation(summary = "User Login")
    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {        
        AuthResponse response = authService.login(request);
        return ApiResponse.success("Login successful", response);
    }
    
    @Operation(summary = "Get Current User Profile")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        UserResponse user = userService.getCurrentUser();
        return ApiResponse.success("Current user profile retrieved successfully", user);
    }
    
    @Operation(summary = "Forgot Password")
    @PostMapping(value = "/forgot-password", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        MessageResponse messageResponse = MessageResponse.builder()
                .message("If your email exists in our system, you will receive a password reset link shortly.")
                .build();
        return ApiResponse.success("Password reset request processed", messageResponse);
    }
    
    @Operation(summary = "Reset Password")
    @PostMapping(value = "/reset-password", consumes = "application/json")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        MessageResponse messageResponse = MessageResponse.builder()
                .message("Password has been reset successfully. You can now login with your new password.")
                .build();
        return ApiResponse.success("Password reset successful", messageResponse);
    }
}
