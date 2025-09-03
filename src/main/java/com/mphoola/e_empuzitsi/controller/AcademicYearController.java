package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.AcademicYearRequest;
import com.mphoola.e_empuzitsi.dto.AcademicYearResponse;
import com.mphoola.e_empuzitsi.service.AcademicYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @ApiResponse(responseCode = "200", description = "Academic years retrieved successfully")
    public ResponseEntity<Page<AcademicYearResponse>> getAllAcademicYears(
            @PageableDefault(size = 20) Pageable pageable,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive) {
        
        log.info("Fetching academic years - pageable: {}, isActive: {}", pageable, isActive);
        
        Page<AcademicYearResponse> academicYears;
        if (isActive != null) {
            academicYears = academicYearService.getAcademicYearsByStatus(isActive, pageable);
        } else {
            academicYears = academicYearService.getAllAcademicYears(pageable);
        }
        
        return ResponseEntity.ok(academicYears);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('list_academic_years')")
    @Operation(summary = "Get all academic years without pagination", 
               description = "Retrieve all academic years as a simple list (no pagination)")
    @ApiResponse(responseCode = "200", description = "Academic years list retrieved successfully")
    public ResponseEntity<List<AcademicYearResponse>> getAllAcademicYearsNoPagination() {
        log.info("Fetching all academic years without pagination");
        
        List<AcademicYearResponse> academicYears = academicYearService.getAllAcademicYearsNoPagination();
        return ResponseEntity.ok(academicYears);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('list_academic_years')")
    @Operation(summary = "Get academic year by ID", description = "Retrieve a specific academic year by its ID")
    @ApiResponse(responseCode = "200", description = "Academic year retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Academic year not found")
    public ResponseEntity<AcademicYearResponse> getAcademicYearById(
            @Parameter(description = "Academic Year ID") @PathVariable Long id) {
        
        log.info("Fetching academic year with id: {}", id);
        
        AcademicYearResponse academicYear = academicYearService.getAcademicYearById(id);
        return ResponseEntity.ok(academicYear);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('list_academic_years')")
    @Operation(summary = "Get academic years statistics", 
               description = "Get statistics about academic years (total, active, inactive counts)")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<AcademicYearService.AcademicYearStatsResponse> getAcademicYearStats() {
        log.info("Fetching academic year statistics");
        
        AcademicYearService.AcademicYearStatsResponse stats = academicYearService.getAcademicYearStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== POST OPERATIONS ====================

    @PostMapping
    @PreAuthorize("hasAuthority('create_academic_year')")
    @Operation(summary = "Create academic year", description = "Create a new academic year")
    @ApiResponse(responseCode = "201", description = "Academic year created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or academic year already exists")
    public ResponseEntity<AcademicYearResponse> createAcademicYear(
            @Valid @RequestBody AcademicYearRequest request) {
        
        log.info("Creating academic year: {}", request);
        
        AcademicYearResponse academicYear = academicYearService.createAcademicYear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(academicYear);
    }

    // ==================== PUT OPERATIONS ====================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_academic_year')")
    @Operation(summary = "Update academic year", description = "Update an existing academic year")
    @ApiResponse(responseCode = "200", description = "Academic year updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or academic year already exists")
    @ApiResponse(responseCode = "404", description = "Academic year not found")
    public ResponseEntity<AcademicYearResponse> updateAcademicYear(
            @Parameter(description = "Academic Year ID") @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequest request) {
        
        log.info("Updating academic year with id: {} with data: {}", id, request);
        
        AcademicYearResponse academicYear = academicYearService.updateAcademicYear(id, request);
        return ResponseEntity.ok(academicYear);
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('activate_academic_year')")
    @Operation(summary = "Toggle academic year status", 
               description = "Toggle the active status of an academic year (activate/deactivate)")
    @ApiResponse(responseCode = "200", description = "Academic year status toggled successfully")
    @ApiResponse(responseCode = "404", description = "Academic year not found")
    public ResponseEntity<AcademicYearResponse> toggleAcademicYearStatus(
            @Parameter(description = "Academic Year ID") @PathVariable Long id) {
        
        log.info("Toggling status for academic year with id: {}", id);
        
        AcademicYearResponse academicYear = academicYearService.toggleAcademicYearStatus(id);
        return ResponseEntity.ok(academicYear);
    }

    // ==================== DELETE OPERATIONS ====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_academic_year')")
    @Operation(summary = "Delete academic year", description = "Delete an academic year")
    @ApiResponse(responseCode = "204", description = "Academic year deleted successfully")
    @ApiResponse(responseCode = "404", description = "Academic year not found")
    @ApiResponse(responseCode = "400", description = "Cannot delete academic year with associated data")
    public ResponseEntity<Void> deleteAcademicYear(
            @Parameter(description = "Academic Year ID") @PathVariable Long id) {
        
        log.info("Deleting academic year with id: {}", id);
        
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.noContent().build();
    }
}
