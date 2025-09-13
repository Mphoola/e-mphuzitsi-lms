package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.dto.subject.SubjectRequest;
import com.mphoola.e_empuzitsi.dto.subject.SubjectResponse;
import com.mphoola.e_empuzitsi.dto.subject.SubjectResponseSimple;
import com.mphoola.e_empuzitsi.service.SubjectService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subject Management", description = "APIs for managing subjects")
public class SubjectController {
    
    private final SubjectService subjectService;
    
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('add_subject')")
    @Operation(summary = "Create a new subject")
    public ResponseEntity<Map<String, Object>> createSubject(@Valid @RequestBody SubjectRequest request) {
        SubjectResponse response = subjectService.createSubject(request);
        return ApiResponse.created(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('update_subject')")
    @Operation(summary = "Update an existing subject")
    public ResponseEntity<Map<String, Object>> updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        SubjectResponse response = subjectService.updateSubject(id, request);
        return ApiResponse.success("Subject updated successfully", response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('delete_subject')")
    @Operation(summary = "Delete a subject")
    public ResponseEntity<Map<String, Object>> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ApiResponse.success("Subject deleted successfully");
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('show_subject_details')")
    @Operation(summary = "Get subject by ID")
    public ResponseEntity<Map<String, Object>> getSubjectById(@PathVariable Long id) {
        SubjectResponse response = subjectService.getSubjectById(id);
        return ApiResponse.success("Subject retrieved successfully", response);
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('list_subjects')")
    @Operation(summary = "Get all subjects")
    public ResponseEntity<Map<String, Object>> getAllSubjects() {
        List<SubjectResponseSimple> response = subjectService.getAllSubjects();
        return ApiResponse.success("Subjects retrieved successfully", response);
    }
    
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAuthority('show_subject_details')")
    @Operation(summary = "Get subject by name")
    public ResponseEntity<Map<String, Object>> getSubjectByName(@PathVariable String name) {
        SubjectResponse response = subjectService.getSubjectByName(name);
        return ApiResponse.success("Subject retrieved successfully", response);
    }
}