package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.entity.ActivityLog;
import com.mphoola.e_empuzitsi.service.ActivityLogService;
import com.mphoola.e_empuzitsi.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('list_audit_logs')") 
    public ResponseEntity<Map<String, Object>> getAllActivityLogs(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String logName,
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String subjectType,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long causerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Page<ActivityLog> activityLogs = activityLogService.findAll(pageable);
        return ApiResponse.success("Activity logs retrieved successfully", activityLogs, "/api/activity-logs");
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Map<String, Object>> getRecentActivityLogs(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> recentLogs = activityLogService.findRecentActivities(pageable);
        return ApiResponse.success("Recent activity logs retrieved successfully", recentLogs, "/api/activity-logs/recent");
    }

    @GetMapping("/by-log-name/{logName}")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Map<String, Object>> getActivityLogsByLogName(
            @PathVariable String logName,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByLogName(logName, pageable);
        return ApiResponse.success("Activity logs by log name retrieved successfully", activityLogs, "/api/activity-logs/by-log-name/" + logName);
    }

    @GetMapping("/by-subject/{subjectType}/{subjectId}")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Map<String, Object>> getActivityLogsBySubject(
            @PathVariable String subjectType,
            @PathVariable Long subjectId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findBySubject(subjectType, subjectId, pageable);
        return ApiResponse.success("Activity logs by subject retrieved successfully", activityLogs, "/api/activity-logs/by-subject/" + subjectType + "/" + subjectId);
    }

    @GetMapping("/by-causer/{causerId}")
    @PreAuthorize("hasAuthority('list_user_audit_log')")
    public ResponseEntity<Map<String, Object>> getActivityLogsByCauser(
            @PathVariable Long causerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByCauser(causerId, pageable);
        return ApiResponse.success("Activity logs by causer retrieved successfully", activityLogs, "/api/activity-logs/by-causer/" + causerId);
    }

    @GetMapping("/by-event/{event}")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Map<String, Object>> getActivityLogsByEvent(
            @PathVariable String event,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByEvent(event, pageable);
        return ApiResponse.success("Activity logs by event retrieved successfully", activityLogs, "/api/activity-logs/by-event/" + event);
    }

    @GetMapping("/by-date-range")
    @PreAuthorize("hasAuthority('list_audit_logs')")
    public ResponseEntity<Map<String, Object>> getActivityLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        Page<ActivityLog> activityLogs = activityLogService.findByDateRange(startDate, endDate, pageable);
        return ApiResponse.success("Activity logs by date range retrieved successfully", activityLogs, "/api/activity-logs/by-date-range");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('see_log_details')")
    public ResponseEntity<Map<String, Object>> getActivityLogById(@PathVariable Long id) {
        ActivityLog activityLog = activityLogService.findAll(Pageable.unpaged())
                .stream()
                .filter(log -> log.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (activityLog == null) {
            return ApiResponse.notFound("Activity log not found");
        }
        
        return ApiResponse.success("Activity log retrieved successfully", activityLog);
    }
}
