package com.mphoola.e_empuzitsi.dto.role;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mphoola.e_empuzitsi.dto.user.UserResponseSimple;

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
    private List<UserResponseSimple> users;
    private Long userCount;
    private Long permissionCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
