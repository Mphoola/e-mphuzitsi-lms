package com.mphoola.e_empuzitsi.dto.subject;

import com.mphoola.e_empuzitsi.annotation.Unique;
import com.mphoola.e_empuzitsi.entity.Subject;
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
public class SubjectRequest {
    
        @NotBlank(message = "Subject name is required")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    @Unique(entity = Subject.class, field = "name", message = "Subject name already exists")
    private String name;
}