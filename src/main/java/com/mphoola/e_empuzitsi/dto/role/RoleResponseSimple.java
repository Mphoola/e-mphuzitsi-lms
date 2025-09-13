package com.mphoola.e_empuzitsi.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseSimple {
    private Long id;
    private String name;
    private Set<PermissionResponse> permissions;
    private Long userCount;
    private Long permissionCount;
}
