# Activity Logging Usage Guide

This Spring Boot Activity Logging system provides comprehensive logging of database events, similar to Laravel's Spatie Activity Log package.

## Features

- **Automatic Logging**: Automatically logs CREATE, UPDATE, and DELETE operations on entities
- **Manual Logging**: Fluent API for custom activity logging
- **Batch Operations**: Group related activities together
- **Rich Queries**: Find activities by user, entity, event type, date range, etc.
- **JSON Properties**: Store additional data as JSON
- **Security Integration**: Automatically captures the current authenticated user

## Automatic Logging

### Enable on Entity

```java
@Entity
@EntityListeners({AuditingEntityListener.class, ActivityLogEntityListener.class})
@Loggable(logName = "user", excludeFields = {"password", "resetToken"})
public class User {
    // ... entity fields
}
```

This will automatically log:
- **CREATE**: When a new user is created
- **UPDATE**: When user data is modified  
- **DELETE**: When a user is deleted

### Loggable Annotation Options

```java
@Loggable(
    logName = "user",           // Custom log name
    logCreate = true,           // Log create operations (default: true)
    logUpdate = true,           // Log update operations (default: true)
    logDelete = true,           // Log delete operations (default: true)
    excludeFields = {"password", "resetToken"}, // Fields to exclude
    includeFields = {"name", "email"}          // Only include specific fields
)
```

## Manual Logging

### Basic Usage

```java
@Autowired
private ActivityLogService activityLogService;

// Simple logging
activityLogService.log("login", "User logged in");

// Fluent API
ActivityLogService.ActivityLogBuilder.create(activityLogService)
    .logName("user")
    .description("User password changed")
    .on(user)  // The subject entity
    .event("password_changed")
    .causedBy(currentUser)  // Who performed the action
    .withProperty("ip_address", "192.168.1.1")
    .log();
```

### Advanced Logging with Properties

```java
Map<String, Object> properties = new HashMap<>();
properties.put("old_email", "old@example.com");
properties.put("new_email", "new@example.com");
properties.put("ip_address", "192.168.1.1");

ActivityLogService.ActivityLogBuilder.create(activityLogService)
    .logName("user")
    .description("User email changed")
    .on(user)
    .event("email_changed")
    .withProperties(properties)
    .log();
```

### Batch Operations

```java
// Option 1: Using withBatch
activityLogService.withBatch(() -> {
    // All logs within this block will share the same batch UUID
    activityLogService.log("bulk_import", "Started bulk user import");
    
    users.forEach(user -> {
        userService.save(user);  // This will trigger automatic logging
    });
    
    activityLogService.log("bulk_import", "Completed bulk user import");
});

// Option 2: Manual batch control
String batchUuid = activityLogService.startBatch();
try {
    // Your operations here
    activityLogService.log("batch_operation", "Processing batch");
    // More operations...
} finally {
    activityLogService.endBatch();
}
```

## Service Layer Integration

### In Your Service Classes

```java
@Service
public class UserService {
    
    @Autowired
    private ActivityLogService activityLogService;
    
    @Autowired
    private UserRepository userRepository;
    
    public User updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        String oldEmail = user.getEmail();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        
        User savedUser = userRepository.save(user);  // Automatic logging
        
        // Additional manual logging
        if (!oldEmail.equals(request.getEmail())) {
            activityLogService.createChangeProperties(
                Map.of("email", oldEmail),
                Map.of("email", request.getEmail())
            );
            
            ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("user")
                .description("Email address changed")
                .on(savedUser)
                .event("email_changed")
                .withProperties(properties)
                .log();
        }
        
        return savedUser;
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        // Manual logging before deletion
        ActivityLogService.ActivityLogBuilder.create(activityLogService)
            .logName("user")
            .description("User account deleted")
            .on(user)
            .event("deleted")
            .withProperty("reason", "Account deactivation requested")
            .log();
            
        userRepository.delete(user);  // This will also trigger automatic logging
    }
}
```

## Querying Activity Logs

### Using the Service

```java
@Autowired
private ActivityLogService activityLogService;

// Get recent activities
Page<ActivityLog> recentLogs = activityLogService.findRecentActivities(pageable);

// Get activities by user
Page<ActivityLog> userLogs = activityLogService.findByCauser(userId, pageable);

// Get activities for specific entity
Page<ActivityLog> entityLogs = activityLogService.findBySubject("User", userId, pageable);

// Get activities by event type
Page<ActivityLog> loginLogs = activityLogService.findByEvent("login", pageable);

// Get activities by log name
Page<ActivityLog> userLogs = activityLogService.findByLogName("user", pageable);

// Get activities by date range
Page<ActivityLog> todayLogs = activityLogService.findByDateRange(
    LocalDateTime.now().withHour(0).withMinute(0),
    LocalDateTime.now().withHour(23).withMinute(59),
    pageable
);
```

### Using the REST API

```bash
# Get all activity logs
GET /api/activity-logs

# Get recent activities
GET /api/activity-logs/recent

# Get activities by log name
GET /api/activity-logs/by-log-name/user

# Get activities for specific entity
GET /api/activity-logs/by-subject/User/123

# Get activities by user
GET /api/activity-logs/by-causer/456

# Get activities by event
GET /api/activity-logs/by-event/login

# Get activities by date range
GET /api/activity-logs/by-date-range?startDate=2025-09-01T00:00:00&endDate=2025-09-03T23:59:59
```

## Activity Log Structure

Each activity log contains:

- **id**: Unique identifier
- **logName**: Category/type of activity (e.g., "user", "role", "system")
- **description**: Human-readable description
- **subjectType**: Class name of the entity being logged
- **subjectId**: ID of the entity being logged
- **event**: Type of event (created, updated, deleted, login, etc.)
- **causerType**: Type of entity that caused the action (usually "User")
- **causerId**: ID of the user who performed the action
- **properties**: JSON object with additional data
- **batchUuid**: Groups related activities together
- **createdAt**: When the activity occurred

## JSON Properties Examples

The `properties` field can contain any JSON data:

```json
{
  "attributes": {
    "name": "John Doe",
    "email": "john@example.com"
  },
  "old": {
    "name": "Jane Doe",
    "email": "jane@example.com"
  },
  "ip_address": "192.168.1.1",
  "user_agent": "Mozilla/5.0...",
  "additional_info": {
    "department": "Engineering",
    "role_changed": true
  }
}
```

## Best Practices

1. **Use Descriptive Log Names**: Group related activities with consistent log names
2. **Include Context**: Add relevant properties like IP address, user agent, etc.
3. **Batch Related Operations**: Use batch logging for operations that modify multiple entities
4. **Exclude Sensitive Data**: Don't log passwords, tokens, or other sensitive information
5. **Clean Up Old Logs**: Consider implementing a cleanup job for old activity logs
6. **Index Properly**: Ensure your database has proper indexes for query performance

## Performance Considerations

- Activity logging runs asynchronously where possible
- Consider archiving old logs to maintain performance
- Use appropriate indexes on frequently queried fields
- Monitor the size of the `properties` JSON field

## Security

- Activity logs automatically capture the current authenticated user
- Sensitive fields can be excluded using the `@Loggable` annotation
- Access to activity logs should be properly secured with appropriate permissions
