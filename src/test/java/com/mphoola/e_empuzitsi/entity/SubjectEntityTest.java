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
 * JPA tests for Subject entity
 * Tests entity creation, unique constraints, and relationships
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SubjectEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void should_create_subject_with_timestamps() {
        // Given
        Subject subject = Subject.builder()
            .name("Physics")
            .build();

        // When
        Subject savedSubject = entityManager.persistAndFlush(subject);

        // Then - verify entity creation and timestamps
        assertThat(savedSubject.getId()).isNotNull();
        assertThat(savedSubject.getName()).isEqualTo("Physics");
        assertThat(savedSubject.getCreatedAt()).isNotNull();
        assertThat(savedSubject.getUpdatedAt()).isNotNull();
        assertThat(savedSubject.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(savedSubject.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_subject_and_modify_timestamps() {
        // Given
        Subject subject = Subject.builder()
            .name("Chemistry")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);
        LocalDateTime originalUpdatedAt = savedSubject.getUpdatedAt();
        
        // Wait for timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - update the subject
        savedSubject.setName("Advanced Chemistry");
        Subject updatedSubject = entityManager.persistAndFlush(savedSubject);

        // Then
        assertThat(updatedSubject.getName()).isEqualTo("Advanced Chemistry");
        assertThat(updatedSubject.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedSubject.getCreatedAt()).isEqualTo(savedSubject.getCreatedAt());
    }

    @Test
    public void should_enforce_unique_name_constraint() {
        // Given - create first subject
        Subject subject1 = Subject.builder()
            .name("Mathematics")
            .build();
        entityManager.persistAndFlush(subject1);

        // When - try to create second subject with same name
        Subject subject2 = Subject.builder()
            .name("Mathematics")
            .build();

        // Then - should throw constraint violation
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> entityManager.persistAndFlush(subject2)
        );
    }

    @Test
    public void should_create_subject_with_lesson_components_relationship() {
        // Given - create subject
        Subject subject = Subject.builder()
            .name("Biology")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        // When - create lesson component for the subject
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Cell Structure")
            .type(LessonType.VIDEO)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        // Then - verify relationship
        assertThat(savedLessonComponent.getId()).isNotNull();
        assertThat(savedLessonComponent.getSubject()).isEqualTo(savedSubject);
        assertThat(savedLessonComponent.getTitle()).isEqualTo("Cell Structure");
        assertThat(savedLessonComponent.getType()).isEqualTo(LessonType.VIDEO);
        assertThat(savedLessonComponent.getCreatedAt()).isNotNull();
        assertThat(savedLessonComponent.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_create_subject_with_student_enrollment_relationship() {
        // Given - create subject, student, and academic year
        Subject subject = Subject.builder()
            .name("Geography")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        User student = User.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .password("password123")
            .build();
        User savedStudent = entityManager.persistAndFlush(student);

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

        // Then - verify relationship
        assertThat(savedStudentSubject.getId()).isNotNull();
        assertThat(savedStudentSubject.getSubject()).isEqualTo(savedSubject);
        assertThat(savedStudentSubject.getStudent()).isEqualTo(savedStudent);
        assertThat(savedStudentSubject.getAcademicYear()).isEqualTo(savedAcademicYear);
        assertThat(savedStudentSubject.getCreatedAt()).isNotNull();
        assertThat(savedStudentSubject.getUpdatedAt()).isNotNull();
    }
}
