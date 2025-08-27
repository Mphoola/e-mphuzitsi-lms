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
 * JPA tests for QuizOption entity
 * Tests entity creation, constraints, relationships, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class QuizOptionEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_quiz_option_with_timestamps() {
        // Given - create required dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Mathematics")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Basic Math")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Math Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is 2 + 2?")
            .quiz(quiz)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.persist(quizQuestion);
        entityManager.flush();

        QuizOption quizOption = QuizOption.builder()
            .optionText("4")
            .isCorrect(true)
            .question(quizQuestion)
            .build();

        // When
        entityManager.persist(quizOption);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(quizOption.getId()).isNotNull();
        assertThat(quizOption.getOptionText()).isEqualTo("4");
        assertThat(quizOption.getIsCorrect()).isTrue();
        assertThat(quizOption.getQuestion()).isEqualTo(quizQuestion);
        assertThat(quizOption.getCreatedAt()).isNotNull();
        assertThat(quizOption.getUpdatedAt()).isNotNull();
        assertThat(quizOption.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(quizOption.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    public void should_update_quiz_option_and_modify_timestamps() {
        // Given - create quiz option
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Physics")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Motion")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Physics Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is the unit of force?")
            .quiz(quiz)
            .build();

        QuizOption quizOption = QuizOption.builder()
            .optionText("Newton")
            .isCorrect(true)
            .question(quizQuestion)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.persist(quizQuestion);
        entityManager.persist(quizOption);
        entityManager.flush();
        
        LocalDateTime initialCreatedAt = quizOption.getCreatedAt();
        LocalDateTime initialUpdatedAt = quizOption.getUpdatedAt();
        
        // Small delay to ensure timestamp difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When - update option
        quizOption.setOptionText("Newtons (N)");
        entityManager.flush();

        // Then - verify updated timestamp
        assertThat(quizOption.getOptionText()).isEqualTo("Newtons (N)");
        assertThat(quizOption.getCreatedAt()).isEqualTo(initialCreatedAt); // Should not change
        assertThat(quizOption.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt); // Should be updated
    }

    @Test
    public void should_require_option_text_field() {
        // Given - create dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Chemistry")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Elements")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Chemistry Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is the symbol for water?")
            .quiz(quiz)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.persist(quizQuestion);
        entityManager.flush();

        // Quiz option without required optionText
        QuizOption quizOption = QuizOption.builder()
            .isCorrect(true)
            .question(quizQuestion)
            .build(); // Missing optionText

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(quizOption);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_is_correct_field() {
        // Given - create dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Biology")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Cells")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Biology Quiz")
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

        // Quiz option without required isCorrect
        QuizOption quizOption = QuizOption.builder()
            .optionText("Mitochondria")
            .question(quizQuestion)
            .build(); // Missing isCorrect

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(quizOption);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_require_question_relationship() {
        // Given - quiz option without required question
        QuizOption quizOption = QuizOption.builder()
            .optionText("Paris")
            .isCorrect(true)
            .build(); // Missing question relationship

        // When/Then - should throw constraint violation
        assertThatThrownBy(() -> {
            entityManager.persist(quizOption);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void should_support_correct_and_incorrect_options() {
        // Given - create dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Geography")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("European Capitals")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Geography Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("What is the capital of France?")
            .quiz(quiz)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.persist(quizQuestion);
        entityManager.flush();

        // Create correct and incorrect options
        QuizOption correctOption = QuizOption.builder()
            .optionText("Paris")
            .isCorrect(true)
            .question(quizQuestion)
            .build();
        
        QuizOption incorrectOption1 = QuizOption.builder()
            .optionText("London")
            .isCorrect(false)
            .question(quizQuestion)
            .build();
        
        QuizOption incorrectOption2 = QuizOption.builder()
            .optionText("Madrid")
            .isCorrect(false)
            .question(quizQuestion)
            .build();

        // When
        entityManager.persist(correctOption);
        entityManager.persist(incorrectOption1);
        entityManager.persist(incorrectOption2);
        entityManager.flush();

        // Then - all options should be created successfully
        assertThat(correctOption.getId()).isNotNull();
        assertThat(incorrectOption1.getId()).isNotNull();
        assertThat(incorrectOption2.getId()).isNotNull();
        assertThat(correctOption.getIsCorrect()).isTrue();
        assertThat(incorrectOption1.getIsCorrect()).isFalse();
        assertThat(incorrectOption2.getIsCorrect()).isFalse();
        assertThat(correctOption.getQuestion()).isEqualTo(quizQuestion);
        assertThat(incorrectOption1.getQuestion()).isEqualTo(quizQuestion);
        assertThat(incorrectOption2.getQuestion()).isEqualTo(quizQuestion);
    }

    @Test
    public void should_support_long_option_text() {
        // Given - create dependencies
        AcademicYear academicYear = AcademicYear.builder()
            .year(2024)
            .build();
        
        Subject subject = Subject.builder()
            .name("Literature")
            .build();
        
        LessonComponent lessonComponent = LessonComponent.builder()
            .title("Classic Novels")
            .type(LessonType.QUIZ)
            .subject(subject)
            .build();
        
        Quiz quiz = Quiz.builder()
            .title("Literature Quiz")
            .lessonComponent(lessonComponent)
            .build();
        
        QuizQuestion quizQuestion = QuizQuestion.builder()
            .questionText("Which of the following best describes the main theme of '1984' by George Orwell?")
            .quiz(quiz)
            .build();

        entityManager.persist(academicYear);
        entityManager.persist(subject);
        entityManager.persist(lessonComponent);
        entityManager.persist(quiz);
        entityManager.persist(quizQuestion);
        entityManager.flush();

        // Long option text
        String longOptionText = "The novel explores the dangers of totalitarianism and the loss of individual freedom in a society " +
            "where the government exercises complete control over its citizens through surveillance, propaganda, and the manipulation " +
            "of language and thought, ultimately serving as a warning about the potential consequences of unchecked political power.";

        QuizOption quizOption = QuizOption.builder()
            .optionText(longOptionText)
            .isCorrect(true)
            .question(quizQuestion)
            .build();

        // When
        entityManager.persist(quizOption);
        entityManager.flush();

        // Then - should handle long text
        assertThat(quizOption.getId()).isNotNull();
        assertThat(quizOption.getOptionText()).isEqualTo(longOptionText);
        assertThat(quizOption.getOptionText().length()).isGreaterThan(300);
    }
}
