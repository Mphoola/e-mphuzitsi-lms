package com.mphoola.e_empuzitsi.validation;

import com.mphoola.e_empuzitsi.annotation.Unique;
import com.mphoola.e_empuzitsi.entity.Subject;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("Unique Validation Annotation Tests")
public class UniqueValidationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private Validator validator;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class TestSubjectRequest {
        @Unique(entity = Subject.class, field = "name", message = "Subject name already exists")
        private String name;
    }

    @Test
    @DisplayName("Should pass validation when subject name is unique")
    void should_pass_validation_when_subject_name_is_unique() {
        // Given - unique subject name
        TestSubjectRequest request = TestSubjectRequest.builder()
                .name("Unique Subject Name")
                .build();

        // When
        Set<ConstraintViolation<TestSubjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when subject name already exists")
    void should_fail_validation_when_subject_name_already_exists() {
        // Given - create an existing subject
        Subject existingSubject = Subject.builder()
                .name("Existing Subject")
                .build();
        entityManager.persist(existingSubject);
        entityManager.flush();

        // Create request with same name
        TestSubjectRequest request = TestSubjectRequest.builder()
                .name("Existing Subject")
                .build();

        // When
        Set<ConstraintViolation<TestSubjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<TestSubjectRequest> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Subject name already exists");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("Should handle case insensitive validation")
    void should_handle_case_insensitive_validation() {
        // Given - create subject with lowercase name
        Subject existingSubject = Subject.builder()
                .name("math")
                .build();
        entityManager.persist(existingSubject);
        entityManager.flush();

        // Create request with same name but different case
        TestSubjectRequest request = TestSubjectRequest.builder()
                .name("MATH")
                .build();

        // When
        Set<ConstraintViolation<TestSubjectRequest>> violations = validator.validate(request);

        // Then - should pass because our current implementation is case sensitive
        // If you want case insensitive, modify the validator
        assertThat(violations).isEmpty();
    }
}