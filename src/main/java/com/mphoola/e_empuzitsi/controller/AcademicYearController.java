package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.academic.AcademicYearRequest;
import com.mphoola.e_empuzitsi.dto.academic.AcademicYearResponse;
import com.mphoola.e_empuzitsi.service.AcademicYearService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/academic-years")
@Tag(name = "Academic Years", description = "Academic year management operations")
@Slf4j
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    // ==================== GET OPERATIONS ====================

    @GetMapping
    @PreAuthorize("hasAuthority('list_academic_years')")
    @Operation(summary = "Get all academic years", description = "Retrieve all academic years with pagination")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academic years retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAllAcademicYears(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive) {
        
        log.info("Fetching academic years - pageable: {}, isActive: {}", pageable, isActive);
        
        Page<AcademicYearResponse> academicYears;
        if (isActive != null) {
            academicYears = academicYearService.getAcademicYearsByStatus(isActive, pageable);
        } else {
            academicYears = academicYearService.getAllAcademicYears(pageable);
        }
        
        return ApiResponse.success("Academic years retrieved successfully", academicYears, "/api/academic-years");
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('list_academic_years')")
    @Operation(summary = "Get all academic years without pagination", 
               description = "Retrieve all academic years as a simple list (no pagination)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academic years list retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAllAcademicYearsNoPagination() {
        log.info("Fetching all academic years without pagination");
        
        List<AcademicYearResponse> academicYears = academicYearService.getAllAcademicYearsNoPagination();
        return ApiResponse.success("Academic years list retrieved successfully", academicYears);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('list_academic_years')")
    @Operation(summary = "Get academic year by ID", description = "Retrieve a specific academic year by its ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academic year retrieved successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Academic year not found")
    public ResponseEntity<Map<String, Object>> getAcademicYearById(
            @Parameter(description = "Academic Year ID") @PathVariable Long id) {
        
        log.info("Fetching academic year with id: {}", id);
        
        AcademicYearResponse academicYear = academicYearService.getAcademicYearById(id);
        return ApiResponse.success("Academic year retrieved successfully", academicYear);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('list_academic_years')")
    @Operation(summary = "Get academic years statistics", 
               description = "Get statistics about academic years (total, active, inactive counts)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAcademicYearStats() {
        log.info("Fetching academic year statistics");
        
        AcademicYearService.AcademicYearStatsResponse stats = academicYearService.getAcademicYearStats();
        return ApiResponse.success("Academic year statistics retrieved successfully", stats);
    }

    // ==================== POST OPERATIONS ====================

    @PostMapping
    @PreAuthorize("hasAuthority('create_academic_year')")
    @Operation(summary = "Create academic year", description = "Create a new academic year")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Academic year created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data or academic year already exists")
    public ResponseEntity<Map<String, Object>> createAcademicYear(
            @Valid @RequestBody AcademicYearRequest request) {
        
        log.info("Creating academic year: {}", request);
        
        AcademicYearResponse academicYear = academicYearService.createAcademicYear(request);
        return ApiResponse.created(academicYear);
    }

    // ==================== PUT OPERATIONS ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_academic_year')")
    @Operation(summary = "Update academic year", description = "Update an existing academic year")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academic year updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data or academic year already exists")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Academic year not found")
    public ResponseEntity<Map<String, Object>> updateAcademicYear(
            @Parameter(description = "Academic Year ID") @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequest request) {
        
        log.info("Updating academic year with id: {} with data: {}", id, request);
        
        AcademicYearResponse academicYear = academicYearService.updateAcademicYear(id, request);
        return ApiResponse.success("Academic year updated successfully", academicYear);
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('activate_academic_year')")
    @Operation(summary = "Toggle academic year status", 
               description = "Toggle the active status of an academic year (activate/deactivate)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Academic year status toggled successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Academic year not found")
    public ResponseEntity<Map<String, Object>> toggleAcademicYearStatus(
            @Parameter(description = "Academic Year ID") @PathVariable Long id) {
        
        log.info("Toggling status for academic year with id: {}", id);
        
        AcademicYearResponse academicYear = academicYearService.toggleAcademicYearStatus(id);
        return ApiResponse.success("Academic year status toggled successfully", academicYear);
    }

    // ==================== DELETE OPERATIONS ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_academic_year')")
    @Operation(summary = "Delete academic year", description = "Delete an academic year")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Academic year deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Academic year not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete academic year with associated data")
    public ResponseEntity<Map<String, Object>> deleteAcademicYear(
            @Parameter(description = "Academic Year ID") @PathVariable Long id) {
        
        log.info("Deleting academic year with id: {}", id);
        
        academicYearService.deleteAcademicYear(id);
        return ApiResponse.success("Academic year deleted successfully");
    }
}
