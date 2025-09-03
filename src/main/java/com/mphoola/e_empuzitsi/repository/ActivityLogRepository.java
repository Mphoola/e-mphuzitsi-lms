package com.mphoola.e_empuzitsi.repository;

import com.mphoola.e_empuzitsi.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    // Find by log name
    Page<ActivityLog> findByLogName(String logName, Pageable pageable);
    
    // Find by subject (entity being logged)
    Page<ActivityLog> findBySubjectTypeAndSubjectId(String subjectType, Long subjectId, Pageable pageable);
    
    // Find by causer (user who performed the action)
    Page<ActivityLog> findByCauserTypeAndCauserId(String causerType, Long causerId, Pageable pageable);
    
    // Find by event type
    Page<ActivityLog> findByEvent(String event, Pageable pageable);
    
    // Find by date range
    Page<ActivityLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Find by batch UUID
    List<ActivityLog> findByBatchUuid(String batchUuid);
    
    // Complex query for filtering
    @Query("SELECT al FROM ActivityLog al WHERE " +
           "(:logName IS NULL OR al.logName = :logName) AND " +
           "(:subjectType IS NULL OR al.subjectType = :subjectType) AND " +
           "(:subjectId IS NULL OR al.subjectId = :subjectId) AND " +
           "(:causerId IS NULL OR al.causerId = :causerId) AND " +
           "(:event IS NULL OR al.event = :event) AND " +
           "(:startDate IS NULL OR al.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR al.createdAt <= :endDate)")
    Page<ActivityLog> findWithFilters(
            @Param("logName") String logName,
            @Param("subjectType") String subjectType,
            @Param("subjectId") Long subjectId,
            @Param("causerId") Long causerId,
            @Param("event") String event,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    // Count activities by user
    @Query("SELECT COUNT(al) FROM ActivityLog al WHERE al.causerId = :causerId")
    Long countByCauserId(@Param("causerId") Long causerId);
    
    // Get recent activities
    @Query("SELECT al FROM ActivityLog al ORDER BY al.createdAt DESC")
    Page<ActivityLog> findRecentActivities(Pageable pageable);
}
