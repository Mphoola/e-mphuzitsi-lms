package com.mphoola.e_empuzitsi.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(health);
        }
    }
    
    @GetMapping("/simple")
    public ResponseEntity<String> simpleCheck() {
        return ResponseEntity.ok("Application is running!");
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> rootHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Application is running!");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
}
