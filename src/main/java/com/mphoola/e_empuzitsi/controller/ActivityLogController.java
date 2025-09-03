package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.entity.ActivityLog;
import com.mphoola.e_empuzitsi.service.ActivityLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('list_audit_logs')") 
    public ResponseEntity<Page<ActivityLog>> getAllActivityLogs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String logName,
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String subjectType,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long causerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Page<ActivityLog> activityLogs = activityLogService.findAll(pageable);
        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Page<ActivityLog>> getRecentActivityLogs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> recentLogs = activityLogService.findRecentActivities(pageable);
        return ResponseEntity.ok(recentLogs);
    }

    @GetMapping("/by-log-name/{logName}")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByLogName(
            @PathVariable String logName,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByLogName(logName, pageable);
        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/by-subject/{subjectType}/{subjectId}")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsBySubject(
            @PathVariable String subjectType,
            @PathVariable Long subjectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findBySubject(subjectType, subjectId, pageable);
        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/by-causer/{causerId}")
    @PreAuthorize("hasAuthority('list_user_audit_log')")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByCauser(
            @PathVariable Long causerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByCauser(causerId, pageable);
        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/by-event/{event}")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByEvent(
            @PathVariable String event,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByEvent(event, pageable);
        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/by-date-range")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Page<ActivityLog>> getActivityLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(activityLogs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('see_log_details')")
    public ResponseEntity<ActivityLog> getActivityLogById(@PathVariable Long id) {
        return activityLogService.findAll(Pageable.unpaged())
                .stream()
                .filter(log -> log.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
