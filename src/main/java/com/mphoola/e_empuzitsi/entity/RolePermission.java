package com.mphoola.e_empuzitsi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermission.RolePermissionId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;
    
    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolePermissionId implements Serializable {
        private Long role;
        private Long permission;
    }
}
