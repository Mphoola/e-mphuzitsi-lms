package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    boolean existsByName(String name);
    
    Optional<Subject> findByName(String name);
    
    @Query("SELECT COUNT(lc) FROM LessonComponent lc WHERE lc.subject.id = :subjectId")
    Long countLessonsBySubjectId(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(ss) FROM StudentSubject ss WHERE ss.subject.id = :subjectId")
    Long countStudentsBySubjectId(@Param("subjectId") Long subjectId);
}