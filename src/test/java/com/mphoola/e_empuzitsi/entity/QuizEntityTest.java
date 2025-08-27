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
 * JPA tests for Quiz entity
 * Tests entity creation, relationships with lesson components, questions, and attempts
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class QuizEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void should_create_quiz_with_timestamps() {
        // Given - create required entities
        Subject subject = Subject.builder()
            .name("Mathematics")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Algebra Basics")
            .type(LessonType.QUIZ)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        // When - create quiz
        Quiz quiz = Quiz.builder()
            .title("Algebra Quiz")
            .lessonComponent(savedLessonComponent)
            .build();
        Quiz savedQuiz = entityManager.persistAndFlush(quiz);

        // Then - verify entity creation and timestamps
        assertThat(savedQuiz.getId()).isNotNull();
        assertThat(savedQuiz.getTitle()).isEqualTo("Algebra Quiz");
        assertThat(savedQuiz.getLessonComponent()).isEqualTo(savedLessonComponent);
        assertThat(savedQuiz.getCreatedAt()).isNotNull();
        assertThat(savedQuiz.getUpdatedAt()).isNotNull();
        assertThat(savedQuiz.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(savedQuiz.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_quiz_and_modify_timestamps() {
        // Given
        Subject subject = Subject.builder()
            .name("Science")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Physics Concepts")
            .type(LessonType.QUIZ)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        Quiz quiz = Quiz.builder()
            .title("Physics Quiz")
            .lessonComponent(savedLessonComponent)
            .build();
        Quiz savedQuiz = entityManager.persistAndFlush(quiz);
        LocalDateTime originalUpdatedAt = savedQuiz.getUpdatedAt();
        
        // Wait for timestamp difference
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - update the quiz
        savedQuiz.setTitle("Advanced Physics Quiz");
        Quiz updatedQuiz = entityManager.persistAndFlush(savedQuiz);

        // Then
        assertThat(updatedQuiz.getTitle()).isEqualTo("Advanced Physics Quiz");
        assertThat(updatedQuiz.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updatedQuiz.getCreatedAt()).isEqualTo(savedQuiz.getCreatedAt());
    }

    @Test
    public void should_create_quiz_with_questions_relationship() {
        // Given - create quiz
        Subject subject = Subject.builder()
            .name("History")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("World Wars")
            .type(LessonType.QUIZ)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        Quiz quiz = Quiz.builder()
            .title("World Wars Quiz")
            .lessonComponent(savedLessonComponent)
            .build();
        Quiz savedQuiz = entityManager.persistAndFlush(quiz);

        // When - create quiz questions
        QuizQuestion question1 = QuizQuestion.builder()
            .questionText("When did World War I start?")
            .quiz(savedQuiz)
            .build();
        QuizQuestion savedQuestion1 = entityManager.persistAndFlush(question1);

        QuizQuestion question2 = QuizQuestion.builder()
            .questionText("When did World War II end?")
            .quiz(savedQuiz)
            .build();
        QuizQuestion savedQuestion2 = entityManager.persistAndFlush(question2);

        // Then - verify relationships
        assertThat(savedQuestion1.getId()).isNotNull();
        assertThat(savedQuestion1.getQuiz()).isEqualTo(savedQuiz);
        assertThat(savedQuestion1.getQuestionText()).isEqualTo("When did World War I start?");

        assertThat(savedQuestion2.getId()).isNotNull();
        assertThat(savedQuestion2.getQuiz()).isEqualTo(savedQuiz);
        assertThat(savedQuestion2.getQuestionText()).isEqualTo("When did World War II end?");
    }

    @Test
    public void should_create_quiz_with_attempts_relationship() {
        // Given - create quiz, user, and academic year
        Subject subject = Subject.builder()
            .name("Geography")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Continents")
            .type(LessonType.QUIZ)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        Quiz quiz = Quiz.builder()
            .title("Continents Quiz")
            .lessonComponent(savedLessonComponent)
            .build();
        Quiz savedQuiz = entityManager.persistAndFlush(quiz);

        User student = User.builder()
            .name("Test Student")
            .email("student@test.com")
            .password("password123")
            .build();
        User savedStudent = entityManager.persistAndFlush(student);

        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();
        AcademicYear savedAcademicYear = entityManager.persistAndFlush(academicYear);

        // When - create quiz attempt
        QuizAttempt attempt = QuizAttempt.builder()
            .quiz(savedQuiz)
            .student(savedStudent)
            .academicYear(savedAcademicYear)
            .score(85)
            .build();
        QuizAttempt savedAttempt = entityManager.persistAndFlush(attempt);

        // Then - verify relationship
        assertThat(savedAttempt.getId()).isNotNull();
        assertThat(savedAttempt.getQuiz()).isEqualTo(savedQuiz);
        assertThat(savedAttempt.getStudent()).isEqualTo(savedStudent);
        assertThat(savedAttempt.getAcademicYear()).isEqualTo(savedAcademicYear);
        assertThat(savedAttempt.getScore()).isEqualTo(85);
        assertThat(savedAttempt.getCreatedAt()).isNotNull();
        assertThat(savedAttempt.getUpdatedAt()).isNotNull();
    }

    @Test
    public void should_require_title_and_lesson_component() {
        // Given
        Subject subject = Subject.builder()
            .name("Biology")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Cell Biology")
            .type(LessonType.QUIZ)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        // When - try to create quiz without title
        Quiz quizNoTitle = Quiz.builder()
            .lessonComponent(savedLessonComponent)
            .build();

        // Then - should throw constraint violation for null title
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> entityManager.persistAndFlush(quizNoTitle)
        );

        // When - try to create quiz without lesson component
        Quiz quizNoLessonComponent = Quiz.builder()
            .title("Orphaned Quiz")
            .build();

        // Then - should throw constraint violation for null lesson component
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> entityManager.persistAndFlush(quizNoLessonComponent)
        );
    }

    @Test
    public void should_enforce_one_to_one_relationship_with_lesson_component() {
        // Given - create lesson component and first quiz
        Subject subject = Subject.builder()
            .name("English")
            .build();
        Subject savedSubject = entityManager.persistAndFlush(subject);

        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Grammar Rules")
            .type(LessonType.QUIZ)
            .subject(savedSubject)
            .build();
        LessonComponent savedLessonComponent = entityManager.persistAndFlush(lessonComponent);

        Quiz quiz1 = Quiz.builder()
            .title("Grammar Quiz 1")
            .lessonComponent(savedLessonComponent)
            .build();
        entityManager.persistAndFlush(quiz1);

        // When - try to create second quiz for same lesson component
        Quiz quiz2 = Quiz.builder()
            .title("Grammar Quiz 2")
            .lessonComponent(savedLessonComponent)
            .build();

        // Then - should throw constraint violation (one-to-one relationship)
        org.junit.jupiter.api.Assertions.assertThrows(
            org.springframework.dao.DataIntegrityViolationException.class,
            () -> entityManager.persistAndFlush(quiz2)
        );
    }
}
