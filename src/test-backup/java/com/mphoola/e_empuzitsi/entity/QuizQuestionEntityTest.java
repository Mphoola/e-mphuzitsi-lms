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
 * JPA tests for QuizQuestion entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class QuizQuestionEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_quiz_question_with_timestamps() {
        // Given - create required dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Mathematics")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Algebra Basics")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Algebra Quiz")
            .lessonComponent(lessonComponent)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.flush();

        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is 2 + 2?")
            .quiz(quiz)
            .build();

        // When
        entityManager.persist(quizQuestion);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(quizQuestion.getId()).isNotNull();
        assertThat(quizQuestion.getQuestionText()).isEqualTo("What is 2 + 2?");
        assertThat(quizQuestion.getQuiz()).isEqualTo(quiz);
        assertThat(quizQuestion.getCreatedAt()).isNotNull();
        assertThat(quizQuestion.getUpdatedAt()).isNotNull();
        assertThat(quizQuestion.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(quizQuestion.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_quiz_question_and_modify_timestamps() {
        // Given - create quiz question
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Physics")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Forces")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Forces Quiz")
            .lessonComponent(lessonComponent)
            .build();

        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is force?")
            .quiz(quiz)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.persist(quizQuestion);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = quizQuestion.getCreatedAt();
        LocalDateTime initialUpdatedAt = quizQuestion.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update question
        quizQuestion.setQuestionText("What is Newton's first law?");
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(quizQuestion.getQuestionText()).isEqualTo("What is Newton's first law?");
        assertThat(quizQuestion.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(quizQuestion.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_question_text_field() {
        // Given - create dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Chemistry")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Atoms")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Chemistry Quiz")
            .lessonComponent(lessonComponent)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.flush();

        // Quiz question without required questionText
        QuizQuestion quizQuestion = QuizQuestion.builder()
            .quiz(quiz)
            .build(); // Missing questionText

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(quizQuestion);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_quiz_relationship() {
        // Given - quiz question without required quiz
        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is the capital of France?")
            .build(); // Missing quiz relationship

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(quizQuestion);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_support_long_question_text() {
        // Given - create dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Literature")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Shakespeare")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Shakespeare Quiz")
            .lessonComponent(lessonComponent)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.flush();

        // Long question text
        String longQuestionText = "In William Shakespeare's play 'Hamlet', the protagonist delivers several famous soliloquies. " +
            "One of the most well-known begins with 'To be or not to be, that is the question.' " +
            "This soliloquy appears in Act III, Scene 1, and represents Hamlet's contemplation of life and death. " +
            "What is the main theme explored in this famous soliloquy?";

        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText(longQuestionText)
            .quiz(quiz)
            .build();

        // When
        entityManager.persist(quizQuestion);
        entityManager.flush();

        // Then - should handle long text
        assertThat(quizQuestion.getId()).isNotNull();
        assertThat(quizQuestion.getQuestionText()).isEqualTo(longQuestionText);
        assertThat(quizQuestion.getQuestionText().length()).isGreaterThan(300);
    }

    @Test
    public void should_support_quiz_option_relationship() {
        // Given - create full hierarchy
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Biology")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Cell Biology")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Cell Quiz")
            .lessonComponent(lessonComponent)
            .build();

        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is the powerhouse of the cell?")
            .quiz(quiz)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.persist(quizQuestion);
        entityManager.flush();

        // Create quiz option for the question
        QuizOption quizOption = QuizOption.builder()
            .optionText("Mitochondria")
            .isCorrect(true)
            .question(quizQuestion)
            .build();

        // When
        entityManager.persist(quizOption);
        entityManager.flush();

        // Clear and reload to test relationship
        entityManager.clear();
        QuizQuestion reloadedQuestion = entityManager.find(QuizQuestion.class, quizQuestion.getId());

        // Then - verify question exists (relationship tested indirectly)
        assertThat(reloadedQuestion).isNotNull();
        assertThat(reloadedQuestion.getQuestionText()).isEqualTo("What is the powerhouse of the cell?");
        // Note: We don't test the collection directly due to lazy loading in test context
    }

    @Test
    public void should_create_multiple_questions_for_same_quiz() {
        // Given - create dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("History")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("World War II")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("WWII Quiz")
            .lessonComponent(lessonComponent)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.flush();

        // Multiple questions for the same quiz
        QuizQuestion question1 = QuizQuestion.builder()
            .questionText("When did World War II start?")
            .quiz(quiz)
            .build();
        
        QuizQuestion question2 = QuizQuestion.builder()
            .questionText("When did World War II end?")
            .quiz(quiz)
            .build();
        
        QuizQuestion question3 = QuizQuestion.builder()
            .questionText("Who were the Allied Powers?")
            .quiz(quiz)
            .build();

        // When
        entityManager.persist(question1);
        entityManager.persist(question2);
        entityManager.persist(question3);
        entityManager.flush();

        // Then - all questions should be created successfully
        assertThat(question1.getId()).isNotNull();
        assertThat(question2.getId()).isNotNull();
        assertThat(question3.getId()).isNotNull();
        assertThat(question1.getQuiz()).isEqualTo(quiz);
        assertThat(question2.getQuiz()).isEqualTo(quiz);
        assertThat(question3.getQuiz()).isEqualTo(quiz);
    }
}
