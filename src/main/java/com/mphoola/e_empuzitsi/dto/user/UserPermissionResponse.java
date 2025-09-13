package com.mphoola.e_empuzitsi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionResponse {
    
    private Long userId;
    private String userName;
    private String userEmail;
    private Long permissionId;
    private String permissionName;
    private LocalDateTime assignedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String source; // "ROLE" or "DIRECT" - indicates if permission comes from role or direct assignment
}
