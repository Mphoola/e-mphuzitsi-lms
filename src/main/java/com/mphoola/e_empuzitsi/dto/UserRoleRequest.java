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
public class UserRoleRequest {
    
    @NotNull(message = "Role ID is required")
    private Long roleId;
    
    private String reason; // Optional reason for the assignment
}
