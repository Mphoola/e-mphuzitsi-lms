package com.mphoola.e_empuzitsi.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @deprecated This class is deprecated. Use ApiResponse utility class for standardized responses.
 * The GlobalExceptionHandler now uses ApiResponse to maintain consistency with all other endpoints.
 * This class is kept for backward compatibility with existing tests only.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private String message;
    private int status;
    private String error;
    private String path;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private List<ValidationError> validationErrors;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
    }
}
