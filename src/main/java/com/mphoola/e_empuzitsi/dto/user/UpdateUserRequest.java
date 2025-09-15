package com.mphoola.e_empuzitsi.dto.user;

import com.mphoola.e_empuzitsi.entity.AccountType;
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
public class UpdateUserRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    /**
     * Account type for the user. Optional field.
     * If not provided, defaults to STUDENT.
     */
    private AccountType accountType;
}