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
    
    // Find by batch UUID
    List<ActivityLog> findByBatchUuid(String batchUuid);
    
    // Complex query for filtering
    @Query("SELECT al FROM ActivityLog al WHERE " +
           "(COALESCE(:logName, '') = '' OR al.logName = :logName) AND " +
           "(COALESCE(:subjectType, '') = '' OR al.subjectType = :subjectType) AND " +
           "(COALESCE(:subjectId, -1) = -1 OR al.subjectId = :subjectId) AND " +
           "(COALESCE(:causerId, -1) = -1 OR al.causerId = :causerId) AND " +
           "(COALESCE(:event, '') = '' OR al.event = :event) AND " +
           "(COALESCE(:startDate, '1970-01-01T00:00:00') = '1970-01-01T00:00:00' OR al.createdAt >= :startDate) AND " +
           "(COALESCE(:endDate, '2999-12-31T23:59:59') = '2999-12-31T23:59:59' OR al.createdAt <= :endDate)")
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
}
