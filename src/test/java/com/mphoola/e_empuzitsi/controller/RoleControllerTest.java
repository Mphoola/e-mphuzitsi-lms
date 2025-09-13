package com.mphoola.e_empuzitsi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mphoola.e_empuzitsi.dto.role.PermissionResponse;
import com.mphoola.e_empuzitsi.dto.role.RoleRequest;
import com.mphoola.e_empuzitsi.dto.role.RoleResponse;
import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import com.mphoola.e_empuzitsi.exception.ResourceConflictException;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.RoleInUseException;
import com.mphoola.e_empuzitsi.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RoleController
 * Tests REST endpoints for role CRUD operations with security
 */
@WebMvcTest(RoleController.class)
@ActiveProfiles("test")
@DisplayName("RoleController Integration Tests")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoleRequest roleRequest;
    private RoleResponse roleResponse;
    private RoleResponse roleResponseWithDetails;

    @BeforeEach
    void setUp() {
        roleRequest = RoleRequest.builder()
                .name("TEST_ROLE")
                .permissionIds(Set.of(1L, 2L))
                .build();

        roleResponse = RoleResponse.builder()
                .id(1L)
                .name("TEST_ROLE")
                .permissions(new HashSet<>())
                .users(new ArrayList<>())
                .userCount(0L)
                .permissionCount(2L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PermissionResponse permission = PermissionResponse.builder()
                .id(1L)
                .name("add_role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserResponse user = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .roles(new HashSet<>())
                .permissions(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        roleResponseWithDetails = RoleResponse.builder()
                .id(1L)
                .name("TEST_ROLE")
                .permissions(Set.of(permission))
                .users(List.of(user))
                .userCount(1L)
                .permissionCount(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(authorities = {"add_role"})
    @DisplayName("Should create role successfully")
    void should_create_role_successfully() throws Exception {
        // Given
        when(roleService.createRole(any(RoleRequest.class))).thenReturn(roleResponse);

        // When & Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("TEST_ROLE"))
                .andExpect(jsonPath("$.userCount").value(0))
                .andExpect(jsonPath("$.permissionCount").value(2));

        verify(roleService).createRole(any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"add_role"})
    @DisplayName("Should return 409 when creating role with existing name")
    void should_return_409_when_creating_role_with_existing_name() throws Exception {
        // Given
        when(roleService.createRole(any(RoleRequest.class)))
                .thenThrow(new ResourceConflictException("Role already exists with name: TEST_ROLE"));

        // When & Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Role already exists with name: TEST_ROLE"));

        verify(roleService).createRole(any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"add_role"})
    @DisplayName("Should return 400 when creating role with invalid request")
    void should_return_400_when_creating_role_with_invalid_request() throws Exception {
        // Given
        RoleRequest invalidRequest = RoleRequest.builder()
                .name("") // Invalid empty name
                .build();

        // When & Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"update_role"})
    @DisplayName("Should update role successfully")
    void should_update_role_successfully() throws Exception {
        // Given
        Long roleId = 1L;
        when(roleService.updateRole(eq(roleId), any(RoleRequest.class))).thenReturn(roleResponse);

        // When & Then
        mockMvc.perform(put("/api/roles/{id}", roleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("TEST_ROLE"));

        verify(roleService).updateRole(eq(roleId), any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"update_role"})
    @DisplayName("Should return 404 when updating non-existent role")
    void should_return_404_when_updating_non_existent_role() throws Exception {
        // Given
        Long roleId = 999L;
        when(roleService.updateRole(eq(roleId), any(RoleRequest.class)))
                .thenThrow(new ResourceNotFoundException("Role not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/roles/{id}", roleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found with id: 999"));

        verify(roleService).updateRole(eq(roleId), any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"delete_role"})
    @DisplayName("Should delete role successfully")
    void should_delete_role_successfully() throws Exception {
        // Given
        Long roleId = 1L;
        doNothing().when(roleService).deleteRole(roleId);

        // When & Then
        mockMvc.perform(delete("/api/roles/{id}", roleId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));

        verify(roleService).deleteRole(roleId);
    }

    @Test
    @WithMockUser(authorities = {"delete_role"})
    @DisplayName("Should return 404 when deleting non-existent role")
    void should_return_404_when_deleting_non_existent_role() throws Exception {
        // Given
        Long roleId = 999L;
        doThrow(new ResourceNotFoundException("Role not found with id: 999"))
                .when(roleService).deleteRole(roleId);

        // When & Then
        mockMvc.perform(delete("/api/roles/{id}", roleId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found with id: 999"));

        verify(roleService).deleteRole(roleId);
    }

    @Test
    @WithMockUser(authorities = {"delete_role"})
    @DisplayName("Should return 409 when deleting role in use")
    void should_return_409_when_deleting_role_in_use() throws Exception {
        // Given
        Long roleId = 1L;
        doThrow(new RoleInUseException("Cannot delete role 'TEST_ROLE' as it is assigned to 2 user(s)"))
                .when(roleService).deleteRole(roleId);

        // When & Then
        mockMvc.perform(delete("/api/roles/{id}", roleId)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete role 'TEST_ROLE' as it is assigned to 2 user(s)"));

        verify(roleService).deleteRole(roleId);
    }

    @Test
    @WithMockUser(authorities = {"show_role_details"})
    @DisplayName("Should get role by ID with details successfully")
    void should_get_role_by_id_with_details_successfully() throws Exception {
        // Given
        Long roleId = 1L;
        when(roleService.getRoleById(roleId)).thenReturn(roleResponseWithDetails);

        // When & Then
        mockMvc.perform(get("/api/roles/{id}", roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("TEST_ROLE"))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions[0].name").value("add_role"))
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.userCount").value(1))
                .andExpect(jsonPath("$.permissionCount").value(1));

        verify(roleService).getRoleById(roleId);
    }

    @Test
    @WithMockUser(authorities = {"show_role_details"})
    @DisplayName("Should return 404 when getting non-existent role by ID")
    void should_return_404_when_getting_non_existent_role_by_id() throws Exception {
        // Given
        Long roleId = 999L;
        when(roleService.getRoleById(roleId))
                .thenThrow(new ResourceNotFoundException("Role not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/roles/{id}", roleId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found with id: 999"));

        verify(roleService).getRoleById(roleId);
    }

    @Test
    @WithMockUser(authorities = {"list_roles"})
    @DisplayName("Should get all roles with counts successfully")
    void should_get_all_roles_with_counts_successfully() throws Exception {
        // Given
        List<RoleResponse> roles = List.of(roleResponse);
        when(roleService.getAllRoles()).thenReturn(roles);

        // When & Then
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("TEST_ROLE"))
                .andExpect(jsonPath("$[0].userCount").value(0))
                .andExpect(jsonPath("$[0].permissionCount").value(2))
                .andExpect(jsonPath("$[0].permissions").isEmpty())
                .andExpect(jsonPath("$[0].users").isEmpty());

        verify(roleService).getAllRoles();
    }

    @Test
    @DisplayName("Should return 403 when accessing endpoint without proper authority")
    void should_return_403_when_accessing_endpoint_without_proper_authority() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isForbidden());

        verify(roleService, never()).createRole(any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"wrong_authority"})
    @DisplayName("Should return 403 when user has wrong authority")
    void should_return_403_when_user_has_wrong_authority() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isForbidden());

        verify(roleService, never()).createRole(any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"add_role"})
    @DisplayName("Should return 400 when request body is missing")
    void should_return_400_when_request_body_is_missing() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any(RoleRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"add_role"})
    @DisplayName("Should return 400 when permission IDs are null")
    void should_return_400_when_permission_ids_are_null() throws Exception {
        // Given
        RoleRequest invalidRequest = RoleRequest.builder()
                .name("VALID_NAME")
                .permissionIds(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(roleService, never()).createRole(any(RoleRequest.class));
    }
}
