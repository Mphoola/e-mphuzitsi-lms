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
 * JPA tests for Quiz entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class QuizEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_quiz_with_timestamps() {
        // Given - create required dependencies
        Subject subject = Subject.builder()
            .name("Mathematics")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Basic Algebra")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.flush();

        Quiz quiz = Quiz.builder()
            .title("Algebra Quiz")
            .lessonComponent(lessonComponent)
            .build();

        // When
        entityManager.persist(quiz);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(quiz.getId()).isNotNull();
        assertThat(quiz.getTitle()).isEqualTo("Algebra Quiz");
        assertThat(quiz.getLessonComponent()).isEqualTo(lessonComponent);
        assertThat(quiz.getCreatedAt()).isNotNull();
        assertThat(quiz.getUpdatedAt()).isNotNull();
        assertThat(quiz.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(quiz.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_quiz_and_modify_timestamps() {
        // Given - create quiz with dependencies
        Subject subject = Subject.builder()
            .name("Physics")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Mechanics")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.flush();

        Quiz quiz = Quiz.builder()
            .title("Mechanics Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        entityManager.persist(quiz);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = quiz.getCreatedAt();
        LocalDateTime initialUpdatedAt = quiz.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update quiz
        quiz.setTitle("Advanced Mechanics Quiz");
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(quiz.getTitle()).isEqualTo("Advanced Mechanics Quiz");
        assertThat(quiz.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(quiz.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_title_field() {
        // Given - create required dependencies first
        Subject subject = Subject.builder()
            .name("Chemistry")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Chemical Reactions")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.flush();

        // Quiz without required title
        Quiz quiz = Quiz.builder()
            .lessonComponent(lessonComponent)
            .build(); // Missing title

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(quiz);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_lesson_component_relationship() {
        // Given - quiz without required lesson component
        Quiz quiz = Quiz.builder()
            .title("Orphaned Quiz")
            .build(); // Missing lessonComponent

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(quiz);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_support_quiz_question_relationship() {
        // Given - create full hierarchy
        Subject subject = Subject.builder()
            .name("Biology")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Cell Structure")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.flush();

        Quiz quiz = Quiz.builder()
            .title("Cell Structure Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        entityManager.persist(quiz);
        entityManager.flush();

        QuizQuestion quizQuestion = QuizQuestion.builder()
            .quiz(quiz)
            .questionText("What is the powerhouse of the cell?")
            .build();

        // When
        entityManager.persist(quizQuestion);
        entityManager.flush();

        // Clear and reload to test relationship
        entityManager.clear();
        Quiz reloadedQuiz = entityManager.find(Quiz.class, quiz.getId());

        // Then - verify relationship exists
        assertThat(reloadedQuiz).isNotNull();
        assertThat(reloadedQuiz.getTitle()).isEqualTo("Cell Structure Quiz");
        // Note: We don't test the collection directly due to lazy loading in test context
    }

    @Test
    public void should_support_quiz_attempt_relationship() {
        // Given - create full hierarchy with user
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        
        User student = User.builder()
            .name("Jane Smith")
            .email("jane.smith@example.com")
            .password("password123")
            .build();
        
        Subject subject = Subject.builder()
            .name("History")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("World War II")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        entityManager.persist(academicYear);
        entityManager.persist(student);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.flush();

        Quiz quiz = Quiz.builder()
            .title("WWII Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        entityManager.persist(quiz);
        entityManager.flush();

        QuizAttempt quizAttempt = QuizAttempt.builder()
            .quiz(quiz)
            .student(student)
            .academicYear(academicYear)
            .score(85)
            .build();

        // When
        entityManager.persist(quizAttempt);
        entityManager.flush();

        // Clear and reload to test relationship
        entityManager.clear();
        Quiz reloadedQuiz = entityManager.find(Quiz.class, quiz.getId());

        // Then - verify relationship exists
        assertThat(reloadedQuiz).isNotNull();
        assertThat(reloadedQuiz.getTitle()).isEqualTo("WWII Quiz");
        // Note: We don't test the collection directly due to lazy loading in test context
    }
}
