package com.mphoola.e_empuzitsi.listener;

import com.mphoola.e_empuzitsi.service.ActivityLogService;
import com.mphoola.e_empuzitsi.util.ApplicationContextProvider;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import java.util.Arrays;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Entity Listener for automatic activity logging
 * This listener will automatically log CREATE, UPDATE, and DELETE operations
 */
public class ActivityLogEntityListener {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogEntityListener.class);
    
    private ActivityLogService getActivityLogService() {
        try {
            return ApplicationContextProvider.getBean(ActivityLogService.class);
        } catch (Exception e) {
            log.debug("ActivityLogService not available: {}", e.getMessage());
            return null;
        }
    }

    private boolean isActivityLogEnabled() {
        try {
            Environment environment = ApplicationContextProvider.getBean(Environment.class);
            // Disable activity logging in test environment
            return !Arrays.asList(environment.getActiveProfiles()).contains("test");
        } catch (Exception e) {
            // If we can't determine the environment, default to enabled
            return true;
        }
    }

    @PostPersist
    public void postPersist(Object entity) {
        if (!isActivityLogEnabled()) return;
        logActivity(entity, "created", "Created " + getEntityName(entity));
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        if (!isActivityLogEnabled()) return;
        logActivity(entity, "updated", "Updated " + getEntityName(entity));
    }

    @PostRemove
    public void postRemove(Object entity) {
        if (!isActivityLogEnabled()) return;
        logActivity(entity, "deleted", "Deleted " + getEntityName(entity));
    }

    private void logActivity(Object entity, String event, String description) {
        try {
            ActivityLogService activityLogService = getActivityLogService();
            if (activityLogService == null) {
                return;
            }

            // Skip logging for ActivityLog entity to avoid recursion
            if (entity.getClass().getSimpleName().equals("ActivityLog")) {
                return;
            }

            Long entityId = getEntityId(entity);
            
            Map<String, Object> properties = new HashMap<>();
            
            // For created/updated events, capture current attributes
            if ("created".equals(event) || "updated".equals(event)) {
                properties.put("attributes", getEntityAttributes(entity));
            }
            
            // If we have an ID, include it in properties
            if (entityId != null) {
                properties.put("id", entityId);
            }

            ActivityLogService.ActivityLogBuilder.create(activityLogService)
                    .description(description)
                    .on(entity)
                    .event(event)
                    .withProperties(properties)
                    .log();

        } catch (Exception e) {
            log.error("Error logging activity for entity {}: {}", entity.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private String getEntityName(Object entity) {
        return entity.getClass().getSimpleName();
    }

    private Long getEntityId(Object entity) {
        try {
            Field idField = findIdField(entity.getClass());
            if (idField != null) {
                idField.setAccessible(true);
                Object id = idField.get(entity);
                if (id instanceof Long) {
                    return (Long) id;
                }
            }
        } catch (Exception e) {
            log.debug("Could not get entity ID for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    private Field findIdField(Class<?> clazz) {
        // First, look for @Id annotation
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        
        // If not found, look for field named "id"
        try {
            return clazz.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            // Check parent class
            if (clazz.getSuperclass() != null) {
                return findIdField(clazz.getSuperclass());
            }
        }
        
        return null;
    }

    private Map<String, Object> getEntityAttributes(Object entity) {
        Map<String, Object> attributes = new HashMap<>();
        
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                // Skip static, transient, and collection fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isTransient(field.getModifiers()) ||
                    field.isAnnotationPresent(Transient.class) ||
                    field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(ManyToMany.class)) {
                    continue;
                }
                
                field.setAccessible(true);
                Object value = field.get(entity);
                
                // Handle ManyToOne and OneToOne relationships by storing just the ID
                if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                    if (value != null) {
                        Long relatedId = getEntityId(value);
                        if (relatedId != null) {
                            attributes.put(field.getName() + "_id", relatedId);
                        }
                    }
                } else {
                    attributes.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            log.debug("Could not get entity attributes for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
        }
        
        return attributes;
    }
}
