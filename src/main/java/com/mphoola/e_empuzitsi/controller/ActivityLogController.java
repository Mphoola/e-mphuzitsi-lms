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
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String subjectType,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long causerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        Page<ActivityLog> activityLogs = activityLogService.findActivityLogsWithFilters(
            event, subjectType, subjectId, causerId, startDate, endDate, pageable);
        return ApiResponse.success("Activity logs retrieved successfully", activityLogs);
    }
}
