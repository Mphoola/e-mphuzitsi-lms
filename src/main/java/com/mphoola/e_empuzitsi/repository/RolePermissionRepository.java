package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermission.RolePermissionId> {
    
    @Query("SELECT rp.permission.name FROM RolePermission rp WHERE rp.role.id = :roleId")
    List<String> findPermissionNamesByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.id = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);
}
