package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.dto.academic.AcademicYearRequest;
import com.mphoola.e_empuzitsi.dto.academic.AcademicYearResponse;
import com.mphoola.e_empuzitsi.entity.AcademicYear;
import com.mphoola.e_empuzitsi.exception.ResourceNotFoundException;
import com.mphoola.e_empuzitsi.exception.ValidationException;
import com.mphoola.e_empuzitsi.repository.AcademicYearRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AcademicYearService {
    
    private final AcademicYearRepository academicYearRepository;
    private final ActivityLogService activityLogService;
    
    public AcademicYearService(AcademicYearRepository academicYearRepository, ActivityLogService activityLogService) {
        this.academicYearRepository = academicYearRepository;
        this.activityLogService = activityLogService;
    }
    
    /**
     * Get all academic years with pagination
     */
    @Transactional(readOnly = true)
    public Page<AcademicYearResponse> getAllAcademicYears(Pageable pageable) {
        log.info("Fetching all academic years with pagination: {}", pageable);
        
        Page<AcademicYear> academicYears = academicYearRepository.findAll(pageable);
        
        return academicYears.map(this::mapToAcademicYearResponse);
    }
    
    /**
     * Get academic years by active status
     */
    @Transactional(readOnly = true)
    public Page<AcademicYearResponse> getAcademicYearsByStatus(Boolean isActive, Pageable pageable) {
        log.info("Fetching academic years by status: {} with pagination: {}", isActive, pageable);
        
        Page<AcademicYear> academicYears = academicYearRepository.findByIsActive(isActive, pageable);
        
        return academicYears.map(this::mapToAcademicYearResponse);
    }
    
    /**
     * Get all academic years without pagination
     */
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAllAcademicYearsNoPagination() {
        log.info("Fetching all academic years without pagination");
        
        List<AcademicYear> academicYears = academicYearRepository.findAllByOrderByYearDesc();
        
        return academicYears.stream()
                .map(this::mapToAcademicYearResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get academic year by ID
     */
    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYearById(Long id) {
        log.info("Fetching academic year with id: {}", id);
        
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));
        
        return mapToAcademicYearResponse(academicYear);
    }
    
    /**
     * Create new academic year
     */
    public AcademicYearResponse createAcademicYear(AcademicYearRequest request) {
        log.info("Creating new academic year: {}", request);
        
        // Validate year uniqueness
        if (academicYearRepository.existsByYear(request.getYear())) {
            throw new ValidationException("Academic year " + request.getYear() + " already exists");
        }
        
        AcademicYear academicYear = AcademicYear.builder()
                .year(request.getYear())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        AcademicYear savedAcademicYear = academicYearRepository.save(academicYear);
        
        // Log activity
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("academic_year_created")
                .description("Created academic year " + savedAcademicYear.getYear())
                .event("academic_year_created")
                .on(savedAcademicYear)
                .withProperty("year", savedAcademicYear.getYear())
                .withProperty("isActive", savedAcademicYear.getIsActive())
                .log();
        
        log.info("Academic year created successfully with id: {}", savedAcademicYear.getId());
        return mapToAcademicYearResponse(savedAcademicYear);
    }
    
    /**
     * Update academic year
     */
    public AcademicYearResponse updateAcademicYear(Long id, AcademicYearRequest request) {
        log.info("Updating academic year with id: {} with data: {}", id, request);
        
        AcademicYear existingAcademicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));
        
        // Check if year is being changed and if new year already exists
        if (!existingAcademicYear.getYear().equals(request.getYear()) && 
            academicYearRepository.existsByYear(request.getYear())) {
            throw new ValidationException("Academic year " + request.getYear() + " already exists");
        }
        
        Integer oldYear = existingAcademicYear.getYear();
        Boolean oldIsActive = existingAcademicYear.getIsActive();
        
        existingAcademicYear.setYear(request.getYear());
        existingAcademicYear.setIsActive(request.getIsActive() != null ? request.getIsActive() : existingAcademicYear.getIsActive());
        
        AcademicYear updatedAcademicYear = academicYearRepository.save(existingAcademicYear);
        
        // Log activity
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("academic_year_updated")
                .description("Updated academic year from " + oldYear + " to " + updatedAcademicYear.getYear())
                .event("academic_year_updated")
                .on(updatedAcademicYear)
                .withProperty("old_year", oldYear)
                .withProperty("new_year", updatedAcademicYear.getYear())
                .withProperty("old_is_active", oldIsActive)
                .withProperty("new_is_active", updatedAcademicYear.getIsActive())
                .log();
        
        log.info("Academic year updated successfully with id: {}", updatedAcademicYear.getId());
        return mapToAcademicYearResponse(updatedAcademicYear);
    }
    
    /**
     * Toggle academic year active status
     */
    public AcademicYearResponse toggleAcademicYearStatus(Long id) {
        log.info("Toggling status for academic year with id: {}", id);
        
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));
        
        Boolean oldStatus = academicYear.getIsActive();
        academicYear.setIsActive(!oldStatus);
        
        AcademicYear updatedAcademicYear = academicYearRepository.save(academicYear);
        
        // Log activity
        String action = updatedAcademicYear.getIsActive() ? "activated" : "deactivated";
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("academic_year_" + action)
                .description("Academic year " + updatedAcademicYear.getYear() + " " + action)
                .event("academic_year_" + action)
                .on(updatedAcademicYear)
                .withProperty("year", updatedAcademicYear.getYear())
                .withProperty("old_status", oldStatus)
                .withProperty("new_status", updatedAcademicYear.getIsActive())
                .log();
        
        log.info("Academic year status toggled successfully for id: {}", updatedAcademicYear.getId());
        return mapToAcademicYearResponse(updatedAcademicYear);
    }
    
    /**
     * Delete academic year
     */
    public void deleteAcademicYear(Long id) {
        log.info("Deleting academic year with id: {}", id);
        
        AcademicYear academicYear = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));
        
        // Check if academic year has associated data
        if (academicYear.getStudentSubjects() != null && !academicYear.getStudentSubjects().isEmpty()) {
            throw new ValidationException("Cannot delete academic year with associated student subjects");
        }
        
        Integer year = academicYear.getYear();
        
        academicYearRepository.delete(academicYear);
        
        // Log activity
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("academic_year_deleted")
                .description("Deleted academic year " + year)
                .event("academic_year_deleted")
                .withProperty("year", year)
                .withProperty("id", id)
                .log();
        
        log.info("Academic year deleted successfully with id: {}", id);
    }
    
    /**
     * Get academic year statistics
     */
    @Transactional(readOnly = true)
    public AcademicYearStatsResponse getAcademicYearStats() {
        log.info("Fetching academic year statistics");
        
        long totalCount = academicYearRepository.count();
        long activeCount = academicYearRepository.countByIsActive(true);
        long inactiveCount = academicYearRepository.countByIsActive(false);
        
        return AcademicYearStatsResponse.builder()
                .totalAcademicYears(totalCount)
                .activeAcademicYears(activeCount)
                .inactiveAcademicYears(inactiveCount)
                .build();
    }
    
    /**
     * Map AcademicYear entity to response DTO
     */
    private AcademicYearResponse mapToAcademicYearResponse(AcademicYear academicYear) {
        Long studentSubjectsCount = 0L;
        if (academicYear.getStudentSubjects() != null) {
            studentSubjectsCount = (long) academicYear.getStudentSubjects().size();
        }
        
        return AcademicYearResponse.builder()
                .id(academicYear.getId())
                .year(academicYear.getYear())
                .isActive(academicYear.getIsActive())
                .studentSubjectsCount(studentSubjectsCount)
                .createdAt(academicYear.getCreatedAt())
                .updatedAt(academicYear.getUpdatedAt())
                .build();
    }
    
    /**
     * Stats response DTO
     */
    public static class AcademicYearStatsResponse {
        private Long totalAcademicYears;
        private Long activeAcademicYears;
        private Long inactiveAcademicYears;
        
        // Constructor, getters, setters, builder
        public AcademicYearStatsResponse() {}
        
        public AcademicYearStatsResponse(Long totalAcademicYears, Long activeAcademicYears, Long inactiveAcademicYears) {
            this.totalAcademicYears = totalAcademicYears;
            this.activeAcademicYears = activeAcademicYears;
            this.inactiveAcademicYears = inactiveAcademicYears;
        }
        
        public static AcademicYearStatsResponseBuilder builder() {
            return new AcademicYearStatsResponseBuilder();
        }
        
        public Long getTotalAcademicYears() { return totalAcademicYears; }
        public void setTotalAcademicYears(Long totalAcademicYears) { this.totalAcademicYears = totalAcademicYears; }
        
        public Long getActiveAcademicYears() { return activeAcademicYears; }
        public void setActiveAcademicYears(Long activeAcademicYears) { this.activeAcademicYears = activeAcademicYears; }
        
        public Long getInactiveAcademicYears() { return inactiveAcademicYears; }
        public void setInactiveAcademicYears(Long inactiveAcademicYears) { this.inactiveAcademicYears = inactiveAcademicYears; }
        
        public static class AcademicYearStatsResponseBuilder {
            private Long totalAcademicYears;
            private Long activeAcademicYears;
            private Long inactiveAcademicYears;
            
            public AcademicYearStatsResponseBuilder totalAcademicYears(Long totalAcademicYears) {
                this.totalAcademicYears = totalAcademicYears;
                return this;
            }
            
            public AcademicYearStatsResponseBuilder activeAcademicYears(Long activeAcademicYears) {
                this.activeAcademicYears = activeAcademicYears;
                return this;
            }
            
            public AcademicYearStatsResponseBuilder inactiveAcademicYears(Long inactiveAcademicYears) {
                this.inactiveAcademicYears = inactiveAcademicYears;
                return this;
            }
            
            public AcademicYearStatsResponse build() {
                return new AcademicYearStatsResponse(totalAcademicYears, activeAcademicYears, inactiveAcademicYears);
            }
        }
    }
}
