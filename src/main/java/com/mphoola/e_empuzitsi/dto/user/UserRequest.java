package com.mphoola.e_empuzitsi.dto.user;

import com.mphoola.e_empuzitsi.annotation.Unique;
import com.mphoola.e_empuzitsi.entity.AccountType;
import com.mphoola.e_empuzitsi.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Unique(entity = User.class, field = "email", message = "Email already exists")
    private String email;
    
    /**
     * Account type for the user. Optional field.
     * If not provided, defaults to STUDENT.
     */
    private AccountType accountType;
}