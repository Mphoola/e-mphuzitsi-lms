package com.mphoola.e_empuzitsi.dto.auth;

import com.mphoola.e_empuzitsi.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private UserResponse user;
    
    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
}
