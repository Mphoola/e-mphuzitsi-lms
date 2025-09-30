package com.mphoola.e_empuzitsi.dto.activity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogResponse {
    
    private Long id;
    private String description;
    private String subjectType;
    private Long subjectId;
    private String event;
    private String causerType;
    private Long causerId;
    private JsonNode properties;
    private LocalDateTime createdAt;
    
    // Additional computed fields
    private String subjectName;
    private String causerName;
}
