package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.userRoles ur " +
           "LEFT JOIN FETCH ur.role r " +
           "LEFT JOIN FETCH r.permissions rp " +
           "LEFT JOIN FETCH u.userPermissions up " +
           "LEFT JOIN FETCH up.permission " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.userRoles ur " +
           "LEFT JOIN FETCH ur.role r " +
           "LEFT JOIN FETCH r.permissions rp " +
           "LEFT JOIN FETCH u.userPermissions up " +
           "LEFT JOIN FETCH up.permission " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);
}
