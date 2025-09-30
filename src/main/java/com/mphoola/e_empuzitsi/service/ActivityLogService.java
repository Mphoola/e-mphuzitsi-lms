package com.mphoola.e_empuzitsi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mphoola.e_empuzitsi.entity.ActivityLog;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    
    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;
    
    public ActivityLogService(ActivityLogRepository activityLogRepository, ObjectMapper objectMapper) {
        this.activityLogRepository = activityLogRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Log an activity with minimal parameters
     */
    public ActivityLog log(String description) {
        return log(description, null);
    }
    
    /**
     * Log an activity with description
     */
    public ActivityLog log(String description, String event) {
        return ActivityLogBuilder.create(this)
                .description(description)
                .event(event != null ? event : "default")
                .log();
    }
    
    /**
     * Internal method to save activity log
     */
    public ActivityLog saveActivityLog(ActivityLog activityLog) {
        try {
            // Set causer from security context if not already set
            if (activityLog.getCauserId() == null) {
                User currentUser = getCurrentUser();
                if (currentUser != null) {
                    activityLog.setCauserType("User");
                    activityLog.setCauserId(currentUser.getId());
                }
            }
            
            return activityLogRepository.save(activityLog);
        } catch (Exception e) {
            log.error("Failed to save activity log: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            log.debug("Could not get current user from security context: {}", e.getMessage());
        }
        return null;
    }
    
    // Query methods
    public Page<ActivityLog> findAll(Pageable pageable) {
        return activityLogRepository.findAll(pageable);
    }
    
    /**
     * Find activity logs with combined filters (following UserController pattern)
     */
    public Page<ActivityLog> findActivityLogsWithFilters(String event, String subjectType, 
                                                        Long subjectId, Long causerId, LocalDateTime startDate, 
                                                        LocalDateTime endDate, Pageable pageable) {
        return activityLogRepository.findWithFilters(subjectType, subjectId, causerId, event, 
                                                    startDate, endDate, pageable);
    }
    
    /**
     * Convert object to JsonNode for properties storage
     */
    public JsonNode toJsonNode(Object obj) {
        try {
            return objectMapper.valueToTree(obj);
        } catch (Exception e) {
            log.warn("Failed to convert object to JsonNode: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }
    
    /**
     * Create properties map with old and new values for updates, with field skipping
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createChangeProperties(Object oldValues, Object newValues, String... skipFields) {
        Map<String, Object> properties = new HashMap<>();
        
        if (oldValues != null && newValues != null) {
            // For updates, compare old and new values and only log changes
            Map<String, Object> oldMap = filterFields((Map<String, Object>) objectMapper.convertValue(oldValues, Map.class), skipFields);
            Map<String, Object> newMap = filterFields((Map<String, Object>) objectMapper.convertValue(newValues, Map.class), skipFields);
            
            // Only include fields that actually changed
            Map<String, Object> oldChanges = new HashMap<>();
            Map<String, Object> newChanges = new HashMap<>();
            
            for (String key : newMap.keySet()) {
                Object oldValue = oldMap.get(key);
                Object newValue = newMap.get(key);
                if (!Objects.equals(oldValue, newValue)) {
                    oldChanges.put(key, oldValue);
                    newChanges.put(key, newValue);
                }
            }
            
            if (!oldChanges.isEmpty() || !newChanges.isEmpty()) {
                properties.put("old", oldChanges);
                properties.put("new", newChanges);
            }
        } else if (newValues != null) {
            // For creation, just include the new values (filtered)
            properties.put("attributes", filterFields((Map<String, Object>) objectMapper.convertValue(newValues, Map.class), skipFields));
        }
        
        return properties;
    }
    
    /**
     * Filter out fields that should be skipped from logging
     */
    private Map<String, Object> filterFields(Map<String, Object> data, String... skipFields) {
        if (skipFields == null || skipFields.length == 0) {
            return data;
        }
        
        Map<String, Object> filtered = new HashMap<>(data);
        for (String field : skipFields) {
            filtered.remove(field);
        }
        return filtered;
    }
    
    /**
     * Builder class for fluent activity logging
     */
    public static class ActivityLogBuilder {
        private final ActivityLogService service;
        private final ActivityLog activityLog;
        
        private ActivityLogBuilder(ActivityLogService service) {
            this.service = service;
            this.activityLog = new ActivityLog();
        }
        
        public static ActivityLogBuilder create(ActivityLogService service) {
            return new ActivityLogBuilder(service);
        }
        
        public ActivityLogBuilder description(String description) {
            activityLog.setDescription(description);
            return this;
        }
        
        public ActivityLogBuilder on(Object subject) {
            if (subject != null) {
                activityLog.setSubjectType(subject.getClass().getSimpleName());
                // Try to get ID using reflection
                try {
                    java.lang.reflect.Field idField = subject.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object id = idField.get(subject);
                    if (id instanceof Long) {
                        activityLog.setSubjectId((Long) id);
                    }
                } catch (Exception e) {
                    // Ignore if we can't get the ID
                }
            }
            return this;
        }
        
        public ActivityLogBuilder causedBy(User user) {
            if (user != null) {
                activityLog.setCauserType("User");
                activityLog.setCauserId(user.getId());
            }
            return this;
        }
        
        public ActivityLogBuilder event(String event) {
            activityLog.setEvent(event);
            return this;
        }
        
        public ActivityLogBuilder withProperties(Object properties) {
            if (properties != null) {
                activityLog.setProperties(service.toJsonNode(properties));
            }
            return this;
        }
        
        public ActivityLogBuilder withProperty(String key, Object value) {
            Map<String, Object> props = new HashMap<>();
            props.put(key, value);
            return withProperties(props);
        }
        
        public ActivityLog log() {
            // Set default description if not provided
            if (activityLog.getDescription() == null) {
                activityLog.setDescription("Activity logged");
            }
            
            return service.saveActivityLog(activityLog);
        }
    }
}
