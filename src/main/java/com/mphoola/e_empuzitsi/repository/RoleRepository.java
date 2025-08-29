package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.Role;
import com.mphoola.e_empuzitsi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);
    
    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);
    
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.role.id = :roleId")
    long countUsersByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT u FROM User u " +
           "JOIN UserRole ur ON u.id = ur.user.id " +
           "WHERE ur.role.id = :roleId")
    List<User> findUsersByRoleId(@Param("roleId") Long roleId);
    
    boolean existsByNameAndIdNot(String name, Long id);
}
