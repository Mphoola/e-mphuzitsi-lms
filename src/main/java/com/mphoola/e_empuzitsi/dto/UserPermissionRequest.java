package com.mphoola.e_empuzitsi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionRequest {
    
    @NotNull(message = "Permission ID is required")
    private Long permissionId;
    
    private String reason; // Optional reason for the assignment
}
