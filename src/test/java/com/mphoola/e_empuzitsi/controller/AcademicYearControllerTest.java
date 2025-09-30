package com.mphoola.e_empuzitsi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mphoola.e_empuzitsi.dto.academic.AcademicYearRequest;
import com.mphoola.e_empuzitsi.dto.academic.AcademicYearResponse;
import com.mphoola.e_empuzitsi.service.AcademicYearService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcademicYearControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AcademicYearService academicYearService;

    @Autowired
    private ObjectMapper objectMapper;

    private AcademicYearResponse academicYearResponse;
    private AcademicYearRequest academicYearRequest;

    @BeforeEach
    void setUp() {
        academicYearResponse = AcademicYearResponse.builder()
                .id(1L)
                .year(2024)
                .isActive(true)
                .studentSubjectsCount(10L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        academicYearRequest = AcademicYearRequest.builder()
                .year(2024)
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser(authorities = "list_academic_years")
    void getAllAcademicYears_ShouldReturnPagedResult() throws Exception {
        // Given
        Page<AcademicYearResponse> page = new PageImpl<>(List.of(academicYearResponse));
        when(academicYearService.getAllAcademicYears(any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/academic-years")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].year").value(2024))
                .andExpect(jsonPath("$.data.data[0].isActive").value(true));

        verify(academicYearService).getAllAcademicYears(any());
    }

    @Test
    @WithMockUser(authorities = "list_academic_years")
    void getAllAcademicYears_WithStatusFilter_ShouldReturnFilteredResult() throws Exception {
        // Given
        Page<AcademicYearResponse> page = new PageImpl<>(List.of(academicYearResponse));
        when(academicYearService.getAcademicYearsByStatus(eq(true), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/academic-years")
                        .param("isActive", "true")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].year").value(2024))
                .andExpect(jsonPath("$.data.data[0].isActive").value(true));

        verify(academicYearService).getAcademicYearsByStatus(eq(true), any());
    }

    @Test
    void getAllAcademicYears_WithoutPermission_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/academic-years"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "list_academic_years")
    void getAllAcademicYearsNoPagination_ShouldReturnList() throws Exception {
        // Given
        when(academicYearService.getAllAcademicYearsNoPagination()).thenReturn(List.of(academicYearResponse));

        // When & Then
        mockMvc.perform(get("/api/academic-years/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].year").value(2024))
                .andExpect(jsonPath("$.data[0].isActive").value(true));

        verify(academicYearService).getAllAcademicYearsNoPagination();
    }

    @Test
    @WithMockUser(authorities = "list_academic_years")
    void getAcademicYearById_ShouldReturnAcademicYear() throws Exception {
        // Given
        when(academicYearService.getAcademicYearById(1L)).thenReturn(academicYearResponse);

        // When & Then
        mockMvc.perform(get("/api/academic-years/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.year").value(2024))
                .andExpect(jsonPath("$.data.isActive").value(true));

        verify(academicYearService).getAcademicYearById(1L);
    }

    @Test
    @WithMockUser(authorities = "list_academic_years")
    void getAcademicYearStats_ShouldReturnStats() throws Exception {
        // Given
        AcademicYearService.AcademicYearStatsResponse stats = 
                AcademicYearService.AcademicYearStatsResponse.builder()
                        .totalAcademicYears(10L)
                        .activeAcademicYears(7L)
                        .inactiveAcademicYears(3L)
                        .build();
        when(academicYearService.getAcademicYearStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/academic-years/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAcademicYears").value(10L))
                .andExpect(jsonPath("$.data.activeAcademicYears").value(7L))
                .andExpect(jsonPath("$.data.inactiveAcademicYears").value(3L));

        verify(academicYearService).getAcademicYearStats();
    }

    @Test
    @WithMockUser(authorities = "create_academic_year")
    void createAcademicYear_ShouldCreateSuccessfully() throws Exception {
        // Given
        when(academicYearService.createAcademicYear(any(AcademicYearRequest.class)))
                .thenReturn(academicYearResponse);

        // When & Then
        mockMvc.perform(post("/api/academic-years")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(academicYearRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.year").value(2024))
                .andExpect(jsonPath("$.data.isActive").value(true));

        verify(academicYearService).createAcademicYear(any(AcademicYearRequest.class));
    }

    @Test
    @WithMockUser(authorities = "create_academic_year")
    void createAcademicYear_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        AcademicYearRequest invalidRequest = AcademicYearRequest.builder()
                .year(null) // Invalid: year cannot be null
                .build();

        // When & Then
        mockMvc.perform(post("/api/academic-years")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createAcademicYear_WithoutPermission_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/academic-years")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(academicYearRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "update_academic_year")
    void updateAcademicYear_ShouldUpdateSuccessfully() throws Exception {
        // Given
        when(academicYearService.updateAcademicYear(eq(1L), any(AcademicYearRequest.class)))
                .thenReturn(academicYearResponse);

        // When & Then
        mockMvc.perform(put("/api/academic-years/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(academicYearRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.year").value(2024))
                .andExpect(jsonPath("$.data.isActive").value(true));

        verify(academicYearService).updateAcademicYear(eq(1L), any(AcademicYearRequest.class));
    }

    @Test
    void updateAcademicYear_WithoutPermission_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/academic-years/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(academicYearRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "activate_academic_year")
    void toggleAcademicYearStatus_ShouldToggleSuccessfully() throws Exception {
        // Given
        AcademicYearResponse toggledResponse = AcademicYearResponse.builder()
                .id(1L)
                .year(2024)
                .isActive(false) // Toggled
                .studentSubjectsCount(10L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(academicYearService.toggleAcademicYearStatus(1L)).thenReturn(toggledResponse);

        // When & Then
        mockMvc.perform(patch("/api/academic-years/1/toggle-status")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));

        verify(academicYearService).toggleAcademicYearStatus(1L);
    }

    @Test
    void toggleAcademicYearStatus_WithoutPermission_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/api/academic-years/1/toggle-status")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "delete_academic_year")
    void deleteAcademicYear_ShouldDeleteSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/academic-years/1")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(academicYearService).deleteAcademicYear(1L);
    }

    @Test
    void deleteAcademicYear_WithoutPermission_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/academic-years/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
