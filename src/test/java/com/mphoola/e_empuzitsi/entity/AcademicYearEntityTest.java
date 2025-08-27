package com.mphoola.e_empuzitsi.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA tests for AcademicYear entity
 * Tests entity creation, constraints, and timestamp functionality
 */
@DataJpaTest
@ActiveProfiles("test")
@EntityScan("com.mphoola.e_empuzitsi.entity")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.generate-ddl=true",
    "spring.jpa.show-sql=true"
})
public class AcademicYearEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void should_create_academic_year_with_timestamps() {
        // Given
        AcademicYear academicYear = AcademicYear.builder()
            .year(2025)
            .isActive(true)
            .build();

        // When
        AcademicYear savedAcademicYear = entityManager.persistAndFlush(academicYear);

        // Then - verify entity creation and timestamps
        assertThat(savedAcademicYear.getId()).isNotNull();
        assertThat(savedAcademicYear.getYear()).isEqualTo(2025);
        assertThat(savedAcademicYear.getIsActive()).isTrue();
        assertThat(savedAcademicYear.getCreatedAt()).isNotNull();
        assertThat(savedAcademicYear.getUpdatedAt()).isNotNull();
        assertThat(savedAcademicYear.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(savedAcademicYear.getUpdatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
