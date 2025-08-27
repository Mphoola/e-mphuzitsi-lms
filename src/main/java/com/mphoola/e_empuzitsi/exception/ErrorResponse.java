package com.mphoola.e_empuzitsi.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
