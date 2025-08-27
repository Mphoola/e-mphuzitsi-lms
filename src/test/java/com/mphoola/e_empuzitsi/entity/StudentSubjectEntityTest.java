package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA tests for StudentSubject entity
 * Tests entity creation, relationships, and unique constraints
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StudentSubjectEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void should_create_student_subject_enrollment_with_timestamps() {
        // Given - create required entities
        User student = User.builder()
            .name("Alice Johnson")
            .email("alice@example.com")
            .password("password123")
            .build();
        User savedStudent = entityManager.persistAndFlush(student);

        Subject subject = Subject.builder()
            .name("English Literature")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        AcademicYear savedAcademicYear = entityManager.persistAndFlush(academicYear);

        // When - create student subject enrollment
        StudentSubject studentSubject = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject)
            .academicYear(savedAcademicYear)
            .build();
        StudentSubject savedStudentSubject = entityManager.persistAndFlush(studentSubject);

        // Then - verify entity creation and timestamps
        assertThat(savedStudentSubject.getId()).isNotNull();
        assertThat(savedStudentSubject.getStudent()).isEqualTo(savedStudent);
        assertThat(savedStudentSubject.getSubject()).isEqualTo(savedSubject);
        assertThat(savedStudentSubject.getAcademicYear()).isEqualTo(savedAcademicYear);
        assertThat(savedStudentSubject.getCreatedAt()).isNotNull();
        assertThat(savedStudentSubject.getUpdatedAt()).isNotNull();
        assertThat(savedStudentSubject.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(savedStudentSubject.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_student_subject_and_modify_timestamps() {
        // Given
        User student = User.builder()
            .name("Bob Smith")
            .email("bob@example.com")
            .password("password123")
            .build();
        User savedStudent = entityManager.persistAndFlush(student);

        Subject subject = Subject.builder()
            .name("History")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        AcademicYear savedAcademicYear = entityManager.persistAndFlush(academicYear);

        StudentSubject studentSubject = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject)
            .academicYear(savedAcademicYear)
            .build();
        StudentSubject savedStudentSubject = entityManager.persistAndFlush(studentSubject);
        LocalDateTime originalUpdatedAt = savedStudentSubject.getUpdatedAt();

        // Wait for timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - update the student subject (change academic year)
        AcademicYear newAcademicYear = AcademicYear.builder()
            .year(2026)
            .isActive(true)
            .build();
        AcademicYear savedNewAcademicYear = entityManager.persistAndFlush(newAcademicYear);
        
        savedStudentSubject.setAcademicYear(savedNewAcademicYear);
        StudentSubject updatedStudentSubject = entityManager.persistAndFlush(savedStudentSubject);

        // Then
        assertThat(updatedStudentSubject.getAcademicYear()).isEqualTo(savedNewAcademicYear);
        assertThat(updatedStudentSubject.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedStudentSubject.getCreatedAt()).isEqualTo(savedStudentSubject.getCreatedAt());
    }

    @Test
    public void should_enforce_unique_student_subject_academic_year_constraint() {
        // Given - create required entities
        User student = User.builder()
            .name("Charlie Brown")
            .email("charlie@example.com")
            .password("password123")
            .build();
        User savedStudent = entityManager.persistAndFlush(student);

        Subject subject = Subject.builder()
            .name("Science")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        AcademicYear savedAcademicYear = entityManager.persistAndFlush(academicYear);

        // Create first enrollment
        StudentSubject studentSubject1 = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject)
            .academicYear(savedAcademicYear)
            .build();
        entityManager.persistAndFlush(studentSubject1);

        // When - try to create duplicate enrollment
        StudentSubject studentSubject2 = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject)
            .academicYear(savedAcademicYear)
            .build();

        // Then - should throw constraint violation
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> entityManager.persistAndFlush(studentSubject2)
        );
    }

    @Test
    public void should_allow_same_student_different_subjects() {
        // Given - create student and multiple subjects
        User student = User.builder()
            .name("Diana Prince")
            .email("diana@example.com")
            .password("password123")
            .build();
        User savedStudent = entityManager.persistAndFlush(student);

        Subject subject1 = Subject.builder()
            .name("Mathematics")
            .build();
        Subject savedSubject1 = entityManager.persistAndFlush(subject1);

        Subject subject2 = Subject.builder()
            .name("Physics")
            .build();
        Subject savedSubject2 = entityManager.persistAndFlush(subject2);

        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        AcademicYear savedAcademicYear = entityManager.persistAndFlush(academicYear);

        // When - enroll same student in different subjects
        StudentSubject enrollment1 = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject1)
            .academicYear(savedAcademicYear)
            .build();
        StudentSubject savedEnrollment1 = entityManager.persistAndFlush(enrollment1);

        StudentSubject enrollment2 = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject2)
            .academicYear(savedAcademicYear)
            .build();
        StudentSubject savedEnrollment2 = entityManager.persistAndFlush(enrollment2);

        // Then - both enrollments should be successful
        assertThat(savedEnrollment1.getId()).isNotNull();
        assertThat(savedEnrollment2.getId()).isNotNull();
        assertThat(savedEnrollment1.getStudent()).isEqualTo(savedStudent);
        assertThat(savedEnrollment2.getStudent()).isEqualTo(savedStudent);
        assertThat(savedEnrollment1.getSubject()).isEqualTo(savedSubject1);
        assertThat(savedEnrollment2.getSubject()).isEqualTo(savedSubject2);
    }

    @Test
    public void should_allow_same_subject_different_academic_years() {
        // Given - create student, subject and multiple academic years
        User student = User.builder()
            .name("Edward Norton")
            .email("edward@example.com")
            .password("password123")
            .build();
        User savedStudent = entityManager.persistAndFlush(student);

        Subject subject = Subject.builder()
            .name("Chemistry")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        AcademicYear academicYear2024 = AcademicYear.builder()
            .year(2024)
            .isActive(false)
            .build();
        AcademicYear savedAcademicYear2024 = entityManager.persistAndFlush(academicYear2024);

        AcademicYear academicYear2025 = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        AcademicYear savedAcademicYear2025 = entityManager.persistAndFlush(academicYear2025);

        // When - enroll same student in same subject for different years
        StudentSubject enrollment2024 = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject)
            .academicYear(savedAcademicYear2024)
            .build();
        StudentSubject savedEnrollment2024 = entityManager.persistAndFlush(enrollment2024);

        StudentSubject enrollment2025 = StudentSubject.builder()
            .student(savedStudent)
            .subject(savedSubject)
            .academicYear(savedAcademicYear2025)
            .build();
        StudentSubject savedEnrollment2025 = entityManager.persistAndFlush(enrollment2025);

        // Then - both enrollments should be successful
        assertThat(savedEnrollment2024.getId()).isNotNull();
        assertThat(savedEnrollment2025.getId()).isNotNull();
        assertThat(savedEnrollment2024.getAcademicYear().getYear()).isEqualTo(2024);
        assertThat(savedEnrollment2025.getAcademicYear().getYear()).isEqualTo(2025);
    }
}
