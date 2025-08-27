package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA tests for AcademicYear entity
 * Tests entity creation, constraints, and timestamp functionality
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AcademicYearEntityTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void should_create_academic_year_with_timestamps() {
        // Given
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();

        // When
        entityManager.persist(academicYear);
        entityManager.flush();

        // Then - verify entity creation and timestamps
        assertThat(academicYear.getId()).isNotNull();
        assertThat(academicYear.getYear()).isEqualTo(2025);
        assertThat(academicYear.getIsActive()).isTrue();
        assertThat(academicYear.getCreatedAt()).isNotNull();
        assertThat(academicYear.getUpdatedAt()).isNotNull();
        assertThat(academicYear.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(academicYear.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
