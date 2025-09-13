package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.academic.AcademicYearRequest;
import com.mphoola.e_empuzitsi.dto.academic.AcademicYearResponse;
import com.mphoola.e_empuzitsi.entity.AcademicYear;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.ValidationException;
import com.mphoola.e_empuzitsi.repository.AcademicYearRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private AcademicYearService academicYearService;

    private AcademicYear academicYear;
    private AcademicYearRequest academicYearRequest;

    @BeforeEach
    void setUp() {
        academicYear = AcademicYear.builder()
                .id(1L)
                .year(2024)
                .isActive(true)
                .studentSubjects(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        academicYearRequest = AcademicYearRequest.builder()
                .year(2024)
                .isActive(true)
                .build();
    }

    @Test
    void getAllAcademicYears_ShouldReturnPagedResult() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<AcademicYear> page = new PageImpl<>(List.of(academicYear));
        when(academicYearRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<AcademicYearResponse> result = academicYearService.getAllAcademicYears(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(2024, result.getContent().get(0).getYear());
        verify(academicYearRepository).findAll(pageable);
    }

    @Test
    void getAcademicYearsByStatus_ShouldReturnFilteredResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<AcademicYear> page = new PageImpl<>(List.of(academicYear));
        when(academicYearRepository.findByIsActive(true, pageable)).thenReturn(page);

        // When
        Page<AcademicYearResponse> result = academicYearService.getAcademicYearsByStatus(true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getIsActive());
        verify(academicYearRepository).findByIsActive(true, pageable);
    }

    @Test
    void getAllAcademicYearsNoPagination_ShouldReturnAllAcademicYears() {
        // Given
        when(academicYearRepository.findAllByOrderByYearDesc()).thenReturn(List.of(academicYear));

        // When
        List<AcademicYearResponse> result = academicYearService.getAllAcademicYearsNoPagination();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).getYear());
        verify(academicYearRepository).findAllByOrderByYearDesc();
    }

    @Test
    void getAcademicYearById_ShouldReturnAcademicYear_WhenExists() {
        // Given
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));

        // When
        AcademicYearResponse result = academicYearService.getAcademicYearById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2024, result.getYear());
        verify(academicYearRepository).findById(1L);
    }

    @Test
    void getAcademicYearById_ShouldThrowException_WhenNotExists() {
        // Given
        when(academicYearRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> academicYearService.getAcademicYearById(1L));
        verify(academicYearRepository).findById(1L);
    }

    @Test
    void createAcademicYear_ShouldCreateSuccessfully_WhenYearDoesNotExist() {
        // Given
        when(academicYearRepository.existsByYear(2024)).thenReturn(false);
        when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(academicYear);

        // When
        AcademicYearResponse result = academicYearService.createAcademicYear(academicYearRequest);

        // Then
        assertNotNull(result);
        assertEquals(2024, result.getYear());
        assertTrue(result.getIsActive());
        verify(academicYearRepository).existsByYear(2024);
        verify(academicYearRepository).save(any(AcademicYear.class));
    }

    @Test
    void createAcademicYear_ShouldThrowException_WhenYearAlreadyExists() {
        // Given
        when(academicYearRepository.existsByYear(2024)).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class, 
                () -> academicYearService.createAcademicYear(academicYearRequest));
        verify(academicYearRepository).existsByYear(2024);
        verify(academicYearRepository, never()).save(any(AcademicYear.class));
    }

    @Test
    void updateAcademicYear_ShouldUpdateSuccessfully_WhenExists() {
        // Given
        AcademicYearRequest updateRequest = AcademicYearRequest.builder()
                .year(2025)
                .isActive(false)
                .build();
        
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));
        when(academicYearRepository.existsByYear(2025)).thenReturn(false);
        when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(academicYear);

        // When
        AcademicYearResponse result = academicYearService.updateAcademicYear(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(academicYearRepository).findById(1L);
        verify(academicYearRepository).existsByYear(2025);
        verify(academicYearRepository).save(any(AcademicYear.class));
    }

    @Test
    void updateAcademicYear_ShouldThrowException_WhenNotExists() {
        // Given
        when(academicYearRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> academicYearService.updateAcademicYear(1L, academicYearRequest));
        verify(academicYearRepository).findById(1L);
    }

    @Test
    void updateAcademicYear_ShouldThrowException_WhenYearAlreadyExists() {
        // Given
        AcademicYearRequest updateRequest = AcademicYearRequest.builder()
                .year(2025)
                .isActive(false)
                .build();
        
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));
        when(academicYearRepository.existsByYear(2025)).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class, 
                () -> academicYearService.updateAcademicYear(1L, updateRequest));
        verify(academicYearRepository).findById(1L);
        verify(academicYearRepository).existsByYear(2025);
    }

    @Test
    void toggleAcademicYearStatus_ShouldToggleSuccessfully() {
        // Given
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));
        when(academicYearRepository.save(any(AcademicYear.class))).thenReturn(academicYear);

        // When
        AcademicYearResponse result = academicYearService.toggleAcademicYearStatus(1L);

        // Then
        assertNotNull(result);
        verify(academicYearRepository).findById(1L);
        verify(academicYearRepository).save(any(AcademicYear.class));
    }

    @Test
    void deleteAcademicYear_ShouldDeleteSuccessfully_WhenNoAssociatedData() {
        // Given
        academicYear.setStudentSubjects(new HashSet<>());
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));

        // When
        academicYearService.deleteAcademicYear(1L);

        // Then
        verify(academicYearRepository).findById(1L);
        verify(academicYearRepository).delete(academicYear);
    }

    @Test
    void deleteAcademicYear_ShouldThrowException_WhenHasAssociatedData() {
        // Given
        academicYear.setStudentSubjects(Collections.singleton(null)); // Non-empty set
        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(academicYear));

        // When & Then
        assertThrows(ValidationException.class, 
                () -> academicYearService.deleteAcademicYear(1L));
        verify(academicYearRepository).findById(1L);
        verify(academicYearRepository, never()).delete(any(AcademicYear.class));
    }

    @Test
    void getAcademicYearStats_ShouldReturnCorrectStats() {
        // Given
        when(academicYearRepository.count()).thenReturn(10L);
        when(academicYearRepository.countByIsActive(true)).thenReturn(7L);
        when(academicYearRepository.countByIsActive(false)).thenReturn(3L);

        // When
        AcademicYearService.AcademicYearStatsResponse result = academicYearService.getAcademicYearStats();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getTotalAcademicYears());
        assertEquals(7L, result.getActiveAcademicYears());
        assertEquals(3L, result.getInactiveAcademicYears());
        verify(academicYearRepository).count();
        verify(academicYearRepository).countByIsActive(true);
        verify(academicYearRepository).countByIsActive(false);
    }
}
