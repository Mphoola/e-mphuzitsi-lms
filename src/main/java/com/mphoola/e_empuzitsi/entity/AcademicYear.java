package com.mphoola.e_empuzitsi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "academic_years")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYear {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Integer year;

    //is active field
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<StudentSubject> studentSubjects;
    
    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<QuizAttempt> quizAttempts;
    
    @OneToMany(mappedBy = "academicYear", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Discussion> discussions;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
