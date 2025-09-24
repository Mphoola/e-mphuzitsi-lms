package com.mphoola.e_empuzitsi.dto.role;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mphoola.e_empuzitsi.annotation.Unique;
import com.mphoola.e_empuzitsi.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleRequest {
    
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Unique(entity = Role.class, field = "name", message = "Role name already exists")
    private String name;
    
    @NotEmpty(message = "Permission IDs are required")
    private Set<Long> permissionIds;
}
