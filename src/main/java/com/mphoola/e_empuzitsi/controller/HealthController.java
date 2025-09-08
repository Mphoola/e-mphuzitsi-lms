package com.mphoola.e_empuzitsi.controller;

import com.mphoola.e_empuzitsi.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test database connection
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5);
                health.put("database", isValid ? "UP" : "DOWN");
                health.put("database_url", connection.getMetaData().getURL());
            }
            
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            
            return ApiResponse.success("Health check completed successfully", health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ApiResponse.error("Health check failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleCheck() {
        return ApiResponse.success("Application is running!");
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> rootHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Application is running!");
        health.put("timestamp", System.currentTimeMillis());
        return ApiResponse.success("Application health check completed", health);
    }
}
