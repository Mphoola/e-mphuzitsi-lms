package com.mphoola.e_empuzitsi.dto.role;

import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Long id;
    private String name;
    private Set<PermissionResponse> permissions;
    private List<UserResponse> users;
    private Long userCount;
    private Long permissionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
