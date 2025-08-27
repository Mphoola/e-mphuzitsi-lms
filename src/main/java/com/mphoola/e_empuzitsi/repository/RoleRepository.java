package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT r FROM Role r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);
}
