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
import java.util.Optional;
import java.util.UUID;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    
    private final ActivityLogRepository activityLogRepository;
    private final ObjectMapper objectMapper;
    
    // Thread-local storage for batch operations
    private static final ThreadLocal<String> currentBatchUuid = new ThreadLocal<>();
    
    public ActivityLogService(ActivityLogRepository activityLogRepository, ObjectMapper objectMapper) {
        this.activityLogRepository = activityLogRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Log an activity with minimal parameters
     */
    public ActivityLog log(String logName) {
        return log(logName, null);
    }
    
    /**
     * Log an activity with description
     */
    public ActivityLog log(String logName, String description) {
        return ActivityLogBuilder.create(this)
                .logName(logName)
                .description(description != null ? description : logName)
                .log();
    }
    
    /**
     * Start a batch operation - all subsequent logs will share the same batch UUID
     */
    public String startBatch() {
        String batchUuid = UUID.randomUUID().toString();
        currentBatchUuid.set(batchUuid);
        return batchUuid;
    }
    
    /**
     * End the current batch operation
     */
    public void endBatch() {
        currentBatchUuid.remove();
    }
    
    /**
     * Execute a block of code within a batch
     */
    public void withBatch(Runnable action) {
        startBatch();
        try {
            action.run();
        } finally {
            endBatch();
        }
    }
    
    /**
     * Internal method to save activity log
     */
    public ActivityLog saveActivityLog(ActivityLog activityLog) {
        try {
            // Set batch UUID if we're in a batch operation
            String batchUuid = currentBatchUuid.get();
            if (batchUuid != null) {
                activityLog.setBatchUuid(batchUuid);
            }
            
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
     * Find activity log by ID
     */
    public Optional<ActivityLog> findById(Long id) {
        return activityLogRepository.findById(id);
    }
    
    /**
     * Find activity logs with combined filters (following UserController pattern)
     */
    public Page<ActivityLog> findActivityLogsWithFilters(String logName, String event, String subjectType, 
                                                        Long subjectId, Long causerId, LocalDateTime startDate, 
                                                        LocalDateTime endDate, Pageable pageable) {
        return activityLogRepository.findWithFilters(logName, subjectType, subjectId, causerId, event, 
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
     * Create properties map with old and new values
     */
    public Map<String, Object> createChangeProperties(Object oldValues, Object newValues) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("attributes", newValues);
        if (oldValues != null) {
            properties.put("old", oldValues);
        }
        return properties;
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
        
        public ActivityLogBuilder logName(String logName) {
            activityLog.setLogName(logName);
            return this;
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
                activityLog.setDescription(activityLog.getLogName());
            }
            
            return service.saveActivityLog(activityLog);
        }
    }
}
