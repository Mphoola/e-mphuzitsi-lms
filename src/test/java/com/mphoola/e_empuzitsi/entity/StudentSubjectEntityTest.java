package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA tests for StudentSubject junction entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class StudentSubjectEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_student_subject_with_timestamps() {
        // Given - create all required dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        User student = User.builder()
            .name("Alice Johnson")
            .email("alice.johnson@example.com")
            .password("password123")
            .build();
        
        Subject subject = Subject.builder()
            .name("Computer Science")
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(student);
        entityManager.persist(subject);
        entityManager.flush();

        StudentSubject studentSubject = StudentSubject.builder()
            .student(student)
            .subject(subject)
            .academicYear(academicYear)
            .build();

        // When
        entityManager.persist(studentSubject);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(studentSubject.getId()).isNotNull();
        assertThat(studentSubject.getStudent()).isEqualTo(student);
        assertThat(studentSubject.getSubject()).isEqualTo(subject);
        assertThat(studentSubject.getAcademicYear()).isEqualTo(academicYear);
        assertThat(studentSubject.getCreatedAt()).isNotNull();
        assertThat(studentSubject.getUpdatedAt()).isNotNull();
        assertThat(studentSubject.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(studentSubject.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_student_subject_and_modify_timestamps() {
        // Given - create student-subject association
        AcademicYear academicYear2024 = AcademicYear.builder()
            .year(2024)
            .isActive(false)
            .build();
        
        AcademicYear academicYear2025 = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        User student = User.builder()
            .name("Bob Wilson")
            .email("bob.wilson@example.com")
            .password("password123")
            .build();
        
        Subject subject = Subject.builder()
            .name("Mathematics")
            .build();

        entityManager.persist(academicYear2024);
        entityManager.persist(academicYear2025);
        entityManager.persist(student);
        entityManager.persist(subject);
        entityManager.flush();

        StudentSubject studentSubject = StudentSubject.builder()
            .student(student)
            .subject(subject)
            .academicYear(academicYear2024)
            .build();
        
        entityManager.persist(studentSubject);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = studentSubject.getCreatedAt();
        LocalDateTime initialUpdatedAt = studentSubject.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update academic year
        studentSubject.setAcademicYear(academicYear2025);
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(studentSubject.getAcademicYear()).isEqualTo(academicYear2025);
        assertThat(studentSubject.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(studentSubject.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_student_relationship() {
        // Given - create dependencies except student
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        Subject subject = Subject.builder()
            .name("Physics")
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.flush();

        // StudentSubject without required student
        StudentSubject studentSubject = StudentSubject.builder()
            .subject(subject)
            .academicYear(academicYear)
            .build(); // Missing student

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(studentSubject);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_subject_relationship() {
        // Given - create dependencies except subject
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        User student = User.builder()
            .name("Carol Davis")
            .email("carol.davis@example.com")
            .password("password123")
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(student);
        entityManager.flush();

        // StudentSubject without required subject
        StudentSubject studentSubject = StudentSubject.builder()
            .student(student)
            .academicYear(academicYear)
            .build(); // Missing subject

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(studentSubject);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_academic_year_relationship() {
        // Given - create dependencies except academic year
        User student = User.builder()
            .name("David Brown")
            .email("david.brown@example.com")
            .password("password123")
            .build();
        
        Subject subject = Subject.builder()
            .name("Chemistry")
            .build();

        entityManager.persist(student);
        entityManager.persist(subject);
        entityManager.flush();

        // StudentSubject without required academic year
        StudentSubject studentSubject = StudentSubject.builder()
            .student(student)
            .subject(subject)
            .build(); // Missing academicYear

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(studentSubject);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_support_multiple_students_in_same_subject() {
        // Given - one subject with multiple students
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        User student1 = User.builder()
            .name("Emma Wilson")
            .email("emma.wilson@example.com")
            .password("password123")
            .build();
        
        User student2 = User.builder()
            .name("Frank Miller")
            .email("frank.miller@example.com")
            .password("password123")
            .build();
        
        Subject subject = Subject.builder()
            .name("Biology")
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(student1);
        entityManager.persist(student2);
        entityManager.persist(subject);
        entityManager.flush();

        StudentSubject studentSubject1 = StudentSubject.builder()
            .student(student1)
            .subject(subject)
            .academicYear(academicYear)
            .build();

        StudentSubject studentSubject2 = StudentSubject.builder()
            .student(student2)
            .subject(subject)
            .academicYear(academicYear)
            .build();

        // When
        entityManager.persist(studentSubject1);
        entityManager.persist(studentSubject2);
        entityManager.flush();

        // Then - both associations should be created successfully
        assertThat(studentSubject1.getId()).isNotNull();
        assertThat(studentSubject2.getId()).isNotNull();
        assertThat(studentSubject1.getSubject()).isEqualTo(subject);
        assertThat(studentSubject2.getSubject()).isEqualTo(subject);
        assertThat(studentSubject1.getStudent()).isEqualTo(student1);
        assertThat(studentSubject2.getStudent()).isEqualTo(student2);
    }

    @Test
    public void should_support_same_student_in_multiple_subjects() {
        // Given - one student with multiple subjects
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        User student = User.builder()
            .name("Grace Taylor")
            .email("grace.taylor@example.com")
            .password("password123")
            .build();
        
        Subject subject1 = Subject.builder()
            .name("History")
            .build();
        
        Subject subject2 = Subject.builder()
            .name("Geography")
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(student);
        entityManager.persist(subject1);
        entityManager.persist(subject2);
        entityManager.flush();

        StudentSubject studentSubject1 = StudentSubject.builder()
            .student(student)
            .subject(subject1)
            .academicYear(academicYear)
            .build();

        StudentSubject studentSubject2 = StudentSubject.builder()
            .student(student)
            .subject(subject2)
            .academicYear(academicYear)
            .build();

        // When
        entityManager.persist(studentSubject1);
        entityManager.persist(studentSubject2);
        entityManager.flush();

        // Then - both associations should be created successfully
        assertThat(studentSubject1.getId()).isNotNull();
        assertThat(studentSubject2.getId()).isNotNull();
        assertThat(studentSubject1.getStudent()).isEqualTo(student);
        assertThat(studentSubject2.getStudent()).isEqualTo(student);
        assertThat(studentSubject1.getSubject()).isEqualTo(subject1);
        assertThat(studentSubject2.getSubject()).isEqualTo(subject2);
    }
}
