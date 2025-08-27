package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    List<UserRole> findByUserId(Long userId);
    
    List<UserRole> findByRoleId(Long roleId);
    
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);
    
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    
    @Query("SELECT ur FROM UserRole ur " +
           "JOIN FETCH ur.role r " +
           "WHERE ur.user.id = :userId")
    List<UserRole> findByUserIdWithRole(@Param("userId") Long userId);
    
    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
