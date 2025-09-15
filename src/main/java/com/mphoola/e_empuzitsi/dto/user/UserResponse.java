package com.mphoola.e_empuzitsi.dto.user;

import com.mphoola.e_empuzitsi.entity.AccountType;
import com.mphoola.e_empuzitsi.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private AccountType accountType;
    private UserStatus status;
    private Set<String> roles;
    private Set<String> permissions;
    @Builder.Default
    private boolean hasVerifiedEmail = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
