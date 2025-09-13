package com.mphoola.e_empuzitsi.dto.academic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request DTO for creating or updating an academic year")
public class AcademicYearRequest {
    
    @NotNull(message = "Year cannot be null")
    @Positive(message = "Year must be a positive number")
    @Schema(description = "Academic year value (e.g., 2024)", example = "2024")
    private Integer year;
    
    @Schema(description = "Whether this academic year is active", example = "true")
    @Builder.Default
    private Boolean isActive = true;
}
