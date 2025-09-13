package com.mphoola.e_empuzitsi.dto.academic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO for academic year information")
public class AcademicYearResponse {
    
    @Schema(description = "Unique identifier of the academic year", example = "1")
    private Long id;
    
    @Schema(description = "Academic year value", example = "2024")
    private Integer year;
    
    @Schema(description = "Whether this academic year is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Number of student subjects in this academic year", example = "15")
    private Long studentSubjectsCount;
    
    @Schema(description = "When the academic year was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "When the academic year was last updated")
    private LocalDateTime updatedAt;
}
