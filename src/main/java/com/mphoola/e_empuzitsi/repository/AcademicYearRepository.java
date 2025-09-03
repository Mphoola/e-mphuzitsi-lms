package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.AcademicYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    
    /**
     * Find academic year by year value
     */
    Optional<AcademicYear> findByYear(Integer year);
    
    /**
     * Check if academic year exists by year value
     */
    boolean existsByYear(Integer year);
    
    /**
     * Find all active academic years
     */
    List<AcademicYear> findByIsActiveTrue();
    
    /**
     * Find all inactive academic years
     */
    List<AcademicYear> findByIsActiveFalse();
    
    /**
     * Find academic years with pagination and filter by active status
     */
    Page<AcademicYear> findByIsActive(Boolean isActive, Pageable pageable);
    
    /**
     * Find academic years with student subjects count
     */
    @Query("SELECT ay, COUNT(ss) as studentSubjectsCount " +
           "FROM AcademicYear ay " +
           "LEFT JOIN ay.studentSubjects ss " +
           "WHERE ay.id = :academicYearId " +
           "GROUP BY ay.id")
    Optional<Object[]> findByIdWithStudentSubjectsCount(@Param("academicYearId") Long academicYearId);
    
    /**
     * Find all academic years ordered by year descending
     */
    List<AcademicYear> findAllByOrderByYearDesc();
    
    /**
     * Find academic years by year range
     */
    @Query("SELECT ay FROM AcademicYear ay WHERE ay.year BETWEEN :startYear AND :endYear ORDER BY ay.year DESC")
    List<AcademicYear> findByYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);
    
    /**
     * Count academic years by active status
     */
    long countByIsActive(Boolean isActive);
}
