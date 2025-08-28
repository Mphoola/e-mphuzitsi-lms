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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth", consumes = "application/json", produces = "application/json")
@Tag(name = "🔐 Authentication", description = "Authentication and user management endpoints")
public class AuthController {
    
    private final AuthService authService;
    private final UserService userService;
    
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }
    
    @Operation(summary = "Register New User")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {        
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "User Login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {        
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get Current User Profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "Forgot Password")
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("If your email exists in our system, you will receive a password reset link shortly.")
                .build());
    }
    
    @Operation(summary = "Reset Password")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Password has been reset successfully. You can now login with your new password.")
                .build());
    }
}
