package com.mphoola.e_empuzitsi.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "subject_type")
    private String subjectType;
    
    @Column(name = "subject_id")
    private Long subjectId;
    
    @Column(name = "event")
    private String event;
    
    @Column(name = "causer_type")
    private String causerType;
    
    @Column(name = "causer_id")
    private Long causerId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "properties", columnDefinition = "json")
    private JsonNode properties;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
