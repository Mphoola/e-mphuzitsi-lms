package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA tests for LessonComponent entity
 * Tests entity creation, lesson type enum, and relationships
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LessonComponentEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void should_create_lesson_component_with_timestamps() {
        // Given
        Subject subject = Subject.builder()
            .name("Computer Science")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Introduction to Programming")
            .type(LessonType.VIDEO)
            .subject(savedSubject)
            .build();

        // When
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        // Then - verify entity creation and timestamps
        assertThat(savedLessonComponent.getId()).isNotNull();
        assertThat(savedLessonComponent.getTitle()).isEqualTo("Introduction to Programming");
        assertThat(savedLessonComponent.getType()).isEqualTo(LessonType.VIDEO);
        assertThat(savedLessonComponent.getSubject()).isEqualTo(savedSubject);
        assertThat(savedLessonComponent.getCreatedAt()).isNotNull();
        assertThat(savedLessonComponent.getUpdatedAt()).isNotNull();
        assertThat(savedLessonComponent.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(savedLessonComponent.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_support_all_lesson_types() {
        // Given
        Subject subject = Subject.builder()
            .name("Mathematics")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        // When & Then - test each lesson type
        for (LessonType type : LessonType.values()) {
            LessonComponent lessonComponent = LessonComponent.builder()
                .title("Test " + type.name())
                .type(type)
                .subject(savedSubject)
                .build();
            
            LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);
            
            assertThat(savedLessonComponent.getType()).isEqualTo(type);
            assertThat(savedLessonComponent.getTitle()).isEqualTo("Test " + type.name());
        }
    }

    @Test
    public void should_update_lesson_component_and_modify_timestamps() {
        // Given
        Subject subject = Subject.builder()
            .name("History")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("World War I")
            .type(LessonType.PDF)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);
        LocalDateTime originalUpdatedAt = savedLessonComponent.getUpdatedAt();
        
        // Wait for timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - update the lesson component
        savedLessonComponent.setTitle("World War I - Complete History");
        savedLessonComponent.setType(LessonType.VIDEO);
        LessonComponent updatedLessonComponent = entityManager.persistAndFlush(savedLessonComponent);

        // Then
        assertThat(updatedLessonComponent.getTitle()).isEqualTo("World War I - Complete History");
        assertThat(updatedLessonComponent.getType()).isEqualTo(LessonType.VIDEO);
        assertThat(updatedLessonComponent.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedLessonComponent.getCreatedAt()).isEqualTo(savedLessonComponent.getCreatedAt());
    }

    @Test
    public void should_create_lesson_component_with_course_content_relationship() {
        // Given - create lesson component
        Subject subject = Subject.builder()
            .name("Physics")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Newton's Laws")
            .type(LessonType.VIDEO)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        // When - create course content for the lesson component
        CourseContent courseContent = CourseContent.builder()
            .fileUrl("https://example.com/newtons-laws.mp4")
            .description("Video explaining Newton's three laws of motion")
            .lessonComponent(savedLessonComponent)
            .build();
        CourseContent savedCourseContent = entityManager.persistAndFlush(courseContent);

        // Then - verify relationship
        assertThat(savedCourseContent.getId()).isNotNull();
        assertThat(savedCourseContent.getLessonComponent()).isEqualTo(savedLessonComponent);
        assertThat(savedCourseContent.getFileUrl()).isEqualTo("https://example.com/newtons-laws.mp4");
        assertThat(savedCourseContent.getDescription()).isEqualTo("Video explaining Newton's three laws of motion");
        assertThat(savedCourseContent.getCreatedAt()).isNotNull();
        assertThat(savedCourseContent.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_create_lesson_component_with_quiz_relationship() {
        // Given - create lesson component
        Subject subject = Subject.builder()
            .name("Chemistry")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Chemical Bonds")
            .type(LessonType.QUIZ)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        // When - create quiz for the lesson component
        Quiz quiz = Quiz.builder()
            .title("Chemical Bonds Quiz")
            .lessonComponent(savedLessonComponent)
            .build();
        Quiz savedQuiz = entityManager.persistAndFlush(quiz);

        // Then - verify relationship
        assertThat(savedQuiz.getId()).isNotNull();
        assertThat(savedQuiz.getLessonComponent()).isEqualTo(savedLessonComponent);
        assertThat(savedQuiz.getTitle()).isEqualTo("Chemical Bonds Quiz");
        assertThat(savedQuiz.getCreatedAt()).isNotNull();
        assertThat(savedQuiz.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_require_title_and_type() {
        // Given
        Subject subject = Subject.builder()
            .name("Biology")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        // When - try to create lesson component without title
        LessonComponent lessonComponentNoTitle = LessonComponent.builder()
            .type(LessonType.PDF)
            .subject(savedSubject)
            .build();

        // Then - should throw constraint violation for null title
        org.junit.jupiter.api.Assertions.assertThrows(
            org.hibernate.exception.ConstraintViolationException.class,
            () -> entityManager.persistAndFlush(lessonComponentNoTitle)
        );

        // When - try to create lesson component without type
        LessonComponent lessonComponentNoType = LessonComponent.builder()
            .title("Cell Division")
            .subject(savedSubject)
            .build();

        // Then - should throw constraint violation for null type
        org.junit.jupiter.api.Assertions.assertThrows(
            org.hibernate.exception.ConstraintViolationException.class,
            () -> entityManager.persistAndFlush(lessonComponentNoType)
        );
    }

    @Test
    public void should_require_subject_relationship() {
        // Given - lesson component without subject
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Orphaned Lesson")
            .type(LessonType.VIDEO)
            .build();

        // When & Then - should throw constraint violation
        org.junit.jupiter.api.Assertions.assertThrows(
            org.hibernate.exception.ConstraintViolationException.class,
            () -> entityManager.persistAndFlush(lessonComponent)
        );
    }
}
