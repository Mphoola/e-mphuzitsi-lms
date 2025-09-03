package com.mphoola.e_empuzitsi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Activity Logging
 */
@Configuration
public class ActivityLogConfig {
    
    /**
     * Configure ObjectMapper for JSON serialization in activity logs
     */
    @Bean
    public ObjectMapper activityLogObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
