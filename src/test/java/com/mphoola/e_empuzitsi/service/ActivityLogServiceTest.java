package com.mphoola.e_empuzitsi.service;

import com.mphoola.e_empuzitsi.entity.ActivityLog;
import com.mphoola.e_empuzitsi.entity.User;
import com.mphoola.e_empuzitsi.repository.ActivityLogRepository;
import com.mphoola.e_empuzitsi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Activity Logging functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ActivityLogServiceTest {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void should_log_manual_activity() {
        // When - log a manual activity
        ActivityLog log = activityLogService.log("test", "Manual test activity");

        // Then
        assertThat(log).isNotNull();
        assertThat(log.getId()).isNotNull();
        assertThat(log.getLogName()).isEqualTo("test");
        assertThat(log.getDescription()).isEqualTo("Manual test activity");
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    public void should_log_activity_with_fluent_api() {
        // Given
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();

        // When - use fluent API
        ActivityLog log = ActivityLogService.ActivityLogBuilder.create(activityLogService)
                .logName("user")
                .description("User profile updated")
                .on(user)
                .event("profile_updated")
                .withProperty("field", "email")
                .log();

        // Then
        assertThat(log).isNotNull();
        assertThat(log.getLogName()).isEqualTo("user");
        assertThat(log.getDescription()).isEqualTo("User profile updated");
        assertThat(log.getEvent()).isEqualTo("profile_updated");
        assertThat(log.getSubjectType()).isEqualTo("User");
        assertThat(log.getProperties()).isNotNull();
    }

    @Test
    public void should_create_batch_operations() {
        // When - execute activities in a batch
        activityLogService.withBatch(() -> {
            activityLogService.log("batch_test", "First activity in batch");
            activityLogService.log("batch_test", "Second activity in batch");
        });

        // Then - both activities should have the same batch UUID
        Page<ActivityLog> logs = activityLogService.findActivityLogsWithFilters("batch_test", null, null, null, null, null, null, PageRequest.of(0, 10));
        assertThat(logs.getContent()).hasSize(2);
        
        String batchUuid = logs.getContent().get(0).getBatchUuid();
        assertThat(batchUuid).isNotNull();
        assertThat(logs.getContent().get(1).getBatchUuid()).isEqualTo(batchUuid);
    }

    @Test
    public void should_query_activities_by_log_name() {
        // Given - create activities with different log names
        activityLogService.log("user", "User activity");
        activityLogService.log("system", "System activity");
        activityLogService.log("user", "Another user activity");

        // When
        Page<ActivityLog> userLogs = activityLogService.findActivityLogsWithFilters("user", null, null, null, null, null, null, PageRequest.of(0, 10));
        Page<ActivityLog> systemLogs = activityLogService.findActivityLogsWithFilters("system", null, null, null, null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(userLogs.getContent()).hasSize(2);
        assertThat(systemLogs.getContent()).hasSize(1);
        
        assertThat(userLogs.getContent()).allMatch(log -> "user".equals(log.getLogName()));
        assertThat(systemLogs.getContent()).allMatch(log -> "system".equals(log.getLogName()));
    }

    @Test 
    public void should_automatically_log_user_creation() {
        // Given - count existing logs
        long initialCount = activityLogRepository.count();

        // When - create a new user (this should trigger automatic logging)
        User user = User.builder()
                .name("Auto Log Test User")
                .email("autolog@example.com")
                .password("password123")
                .build();
        
        userRepository.save(user);

        // Then - should have one more activity log
        long finalCount = activityLogRepository.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);

        // Find the created log
        Page<ActivityLog> recentLogs = activityLogService.findActivityLogsWithFilters(null, null, null, null, null, null, null, PageRequest.of(0, 1));
        assertThat(recentLogs.getContent()).hasSize(1);
        
        ActivityLog log = recentLogs.getContent().get(0);
        assertThat(log.getEvent()).isEqualTo("created");
        assertThat(log.getSubjectType()).isEqualTo("User");
        assertThat(log.getSubjectId()).isEqualTo(user.getId());
        assertThat(log.getDescription()).contains("Created User");
    }
}
