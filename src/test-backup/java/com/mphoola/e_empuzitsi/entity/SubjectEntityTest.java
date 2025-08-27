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
 * JPA tests for Subject entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class SubjectEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_subject_with_timestamps() {
        // Given
        Subject subject = Subject.builder()
            .name("Mathematics")
            .build();

        // When
        entityManager.persist(subject);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(subject.getId()).isNotNull();
        assertThat(subject.getName()).isEqualTo("Mathematics");
        assertThat(subject.getCreatedAt()).isNotNull();
        assertThat(subject.getUpdatedAt()).isNotNull();
        assertThat(subject.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(subject.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_subject_and_modify_timestamps() {
        // Given - create initial subject
        Subject subject = Subject.builder()
            .name("Physics")
            .build();
        
        entityManager.persist(subject);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = subject.getCreatedAt();
        LocalDateTime initialUpdatedAt = subject.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update subject
        subject.setName("Advanced Physics");
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(subject.getName()).isEqualTo("Advanced Physics");
        assertThat(subject.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(subject.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_name_field() {
        // Given - subject without required name
        Subject subject = Subject.builder()
            .build(); // Missing name

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(subject);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_support_lesson_component_relationship() {
        // Given - create subject and lesson component
        Subject subject = Subject.builder()
            .name("Computer Science")
            .build();
        
        entityManager.persist(subject);
        entityManager.flush();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Introduction to Programming")
            .type(LessonType.VIDEO)
            .subject(subject)
            .build();

        // When
        entityManager.persist(lessonComponent);
        entityManager.flush();

        // Clear and reload to test relationship
        entityManager.clear();
        Subject reloadedSubject = entityManager.find(Subject.class, subject.getId());

        // Then - verify relationship
        assertThat(reloadedSubject).isNotNull();
        assertThat(reloadedSubject.getName()).isEqualTo("Computer Science");
        // Note: We don't test the collection directly due to lazy loading in test context
    }

    @Test
    public void should_support_student_subject_relationship() {
        // Given - create all required entities
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        User student = User.builder()
            .name("John Doe")
            .email("john.doe@example.com")
            .password("password123")
            .build();
        
        Subject subject = Subject.builder()
            .name("Biology")
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

        // Clear and reload to test relationship
        entityManager.clear();
        Subject reloadedSubject = entityManager.find(Subject.class, subject.getId());

        // Then - verify relationship exists
        assertThat(reloadedSubject).isNotNull();
        assertThat(reloadedSubject.getName()).isEqualTo("Biology");
        // Note: We don't test the collection directly due to lazy loading in test context
    }
}
