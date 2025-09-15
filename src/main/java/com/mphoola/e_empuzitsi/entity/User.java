package com.mphoola.e_empuzitsi.entity;

import com.mphoola.e_empuzitsi.annotation.Loggable;
import com.mphoola.e_empuzitsi.listener.ActivityLogEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@EntityListeners({AuditingEntityListener.class, ActivityLogEntityListener.class})
@Loggable(logName = "user", excludeFields = {"password", "resetToken"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    @Builder.Default
    private AccountType accountType = AccountType.STUDENT;
    
    // User status field - determines if user can access the system
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    // Email verification fields
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;
    
    @Column(name = "verification_token")
    private String verificationToken;
    
    // Password reset fields
    @Column(name = "reset_token")
    private String resetToken;
    
    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;
    
    // Relationships
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<StudentSubject> studentSubjects;
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DiscussionPost> discussionPosts;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<QuizAttempt> quizAttempts;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserPermission> userPermissions;
    
    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper methods for email verification
    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }
    
    public void markEmailAsVerified() {
        this.emailVerifiedAt = LocalDateTime.now();
        this.verificationToken = null;
    }

}
