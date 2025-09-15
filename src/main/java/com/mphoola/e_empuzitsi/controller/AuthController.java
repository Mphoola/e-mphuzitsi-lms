package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.auth.AuthResponse;
import com.mphoola.e_empuzitsi.dto.auth.LoginRequest;
import com.mphoola.e_empuzitsi.dto.auth.RegisterRequest;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.dto.auth.ForgotPasswordRequest;
import com.mphoola.e_empuzitsi.dto.auth.ResetPasswordRequest;
import com.mphoola.e_empuzitsi.security.AllowUnverifiedEmail;
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
    @AllowUnverifiedEmail
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {        
        AuthResponse response = authService.register(request);
        return ApiResponse.success("User registered successfully", response);
    }
    
    @Operation(summary = "User Login")
    @PostMapping(value = "/login", consumes = "application/json")
    @AllowUnverifiedEmail
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {        
        AuthResponse response = authService.login(request);
        return ApiResponse.success("Login successful", response);
    }
    
    @Operation(summary = "Get Current User Profile")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            UserResponse user = userService.getCurrentUser();
            return ApiResponse.success("Current user profile retrieved successfully", user);
        } catch (Exception e) {
            // Return normal response even if user doesn't exist
            return ApiResponse.success("User profile request processed", null);
        }
    }
    
    @Operation(summary = "Forgot Password")
    @PostMapping(value = "/forgot-password", consumes = "application/json")
    @AllowUnverifiedEmail
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.forgotPassword(request.getEmail());
        } catch (Exception e) {
            // Skip error, return normal response even if user doesn't exist
        }
        return ApiResponse.success("If your email exists in our system, you will receive a password reset link shortly.");
    }
    
    @Operation(summary = "Reset Password")
    @PostMapping(value = "/reset-password", consumes = "application/json")
    @AllowUnverifiedEmail
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.success("Password reset successful");
    }
    
    @Operation(summary = "Verify Email Address")
    @PostMapping("/verify-email")
    @AllowUnverifiedEmail
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyEmail(token);
            return ApiResponse.success("Email verification successful");
        } catch (Exception e) {
            return ApiResponse.success("Verification request not processed");
        }
    }
    
    @Operation(summary = "Resend Verification Email")
    @PostMapping("/resend-verification")
    @AllowUnverifiedEmail
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestParam String email) {
        try {
            userService.resendEmailVerification(email);
            
            return ApiResponse.success("Verification email sent");
        } catch (Exception e) {
            return ApiResponse.success("Verification request not processed. System busy.");
        }
    }
}
