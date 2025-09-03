package com.mphoola.e_empuzitsi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessResponse {
    
    private Long userId;
    private String userName;
    private String userEmail;
    private List<UserRoleResponse> roles;
    private List<UserPermissionResponse> permissions;
    private List<String> effectivePermissions; // All permissions the user has (from roles + direct)
}
