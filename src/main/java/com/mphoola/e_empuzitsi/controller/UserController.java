package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.user.UpdateUserRequest;
import com.mphoola.e_empuzitsi.dto.user.UserRequest;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.dto.user.UserResponseSimple;
import com.mphoola.e_empuzitsi.entity.AccountType;
import com.mphoola.e_empuzitsi.entity.UserStatus;
import com.mphoola.e_empuzitsi.service.UserService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('add_user')")
    @Operation(summary = "Create a new user")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ApiResponse.created(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_user')")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ApiResponse.success("User updated successfully", response);
    }
    
    @PutMapping("/{id}/ban")
    @PreAuthorize("hasAuthority('ban_user')")
    @Operation(summary = "Ban a user")
    public ResponseEntity<Map<String, Object>> banUser(@PathVariable Long id) {
        userService.banUser(id);
        return ApiResponse.success("User banned successfully");
    }
    
    @PutMapping("/{id}/unban")
    @PreAuthorize("hasAuthority('unban_user')")
    @Operation(summary = "Unban a user")
    public ResponseEntity<Map<String, Object>> unbanUser(@PathVariable Long id) {
        userService.unbanUser(id);
        return ApiResponse.success("User unbanned successfully");
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('show_user_details')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ApiResponse.success("User retrieved successfully", response);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('list_users')")
    @Operation(summary = "Get all users with pagination, filtering, sorting, and searching")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AccountType accountType,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable) {
        
        Page<UserResponseSimple> response = userService.getAllUsers(search, accountType, status, pageable);
        return ApiResponse.success("Users retrieved successfully", response);
    }
    
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('show_user_details')")
    @Operation(summary = "Get user by email")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ApiResponse.success("User retrieved successfully", response);
    }
}