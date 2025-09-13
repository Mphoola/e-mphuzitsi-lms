package com.mphoola.e_empuzitsi.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating standardized API responses
 * Following Laravel response-builder pattern: { message, errors, data }
 */
public class ApiResponse {
    
    /**
     * Create a successful response with data
     */
    public static <T> ResponseEntity<Map<String, Object>> success(String message, T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errors", new HashMap<>());
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a successful response with custom data key
     */
    public static <T> ResponseEntity<Map<String, Object>> success(String message, T data, String key) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errors", new HashMap<>());
        response.put(key, data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a successful response with paginated data
     */
    public static <T> ResponseEntity<Map<String, Object>> success(String message, Page<T> page, String basePath) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errors", new HashMap<>());
        response.put("data", createPaginatedData(page, basePath));
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a successful response without data
     */
    public static ResponseEntity<Map<String, Object>> success(String message) {
        return success(message, null);
    }
    
    /**
     * Create an error response
     */
    public static ResponseEntity<Map<String, Object>> error(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errors", new HashMap<>());
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Create an error response with validation errors
     */
    public static ResponseEntity<Map<String, Object>> error(String message, Map<String, Object> errors, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errors", errors);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Create a not found response
     */
    public static ResponseEntity<Map<String, Object>> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Create a bad request response
     */
    public static ResponseEntity<Map<String, Object>> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Create a bad request response with validation errors
     */
    public static ResponseEntity<Map<String, Object>> badRequest(String message, Map<String, Object> errors) {
        return error(message, errors, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Create an unauthorized response
     */
    public static ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return error(message, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Create a forbidden response
     */
    public static ResponseEntity<Map<String, Object>> forbidden(String message) {
        return error(message, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Create an internal server error response
     */
    public static ResponseEntity<Map<String, Object>> serverError(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Laravel-style convenience methods
     */
    
    /**
     * Create an "ok" response with data 
     */
    public static <T> ResponseEntity<Map<String, Object>> ok(T data) {
        return success("Completed successfully", data);
    }
    
    /**
     * Create an "ok" response without data
     */
    public static ResponseEntity<Map<String, Object>> ok() {
        return success("Completed successfully");
    }
    
    /**
     * Create a "created" response 
     */
    public static <T> ResponseEntity<Map<String, Object>> created(T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Resource created");
        response.put("errors", new HashMap<>());
        response.put("data", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Create an "accepted" response 
     */
    public static <T> ResponseEntity<Map<String, Object>> accepted(T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Request accepted and processing");
        response.put("errors", new HashMap<>());
        response.put("data", data);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    /**
     * Create a "failed" response 
     */
    public static ResponseEntity<Map<String, Object>> failed(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Create an "unprocessable" response 
     */
    public static ResponseEntity<Map<String, Object>> unprocessable(String message) {
        return error(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    /**
     * Create an "unprocessable" response with validation errors
     */
    public static ResponseEntity<Map<String, Object>> unprocessable(String message, Map<String, Object> errors) {
        return error(message, errors, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    /**
     * Create a conflict response
     */
    public static ResponseEntity<Map<String, Object>> conflict(String message) {
        return error(message, HttpStatus.CONFLICT);
    }
    
    /**
     * Create a method not allowed response
     */
    public static ResponseEntity<Map<String, Object>> methodNotAllowed(String message) {
        return error(message, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    /**
     * Create an unsupported media type response
     */
    public static ResponseEntity<Map<String, Object>> unsupportedMediaType(String message) {
        return error(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    
    /**
     * Create an error response with meta information (path, timestamp, etc.)
     * Simplified version without success flag to match Laravel pattern
     */
    public static ResponseEntity<Map<String, Object>> errorWithMeta(String message, HttpStatus status, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errors", new HashMap<>());
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Create an error response with validation errors 
     */
    public static ResponseEntity<Map<String, Object>> errorWithMeta(String message, Map<String, Object> errors, HttpStatus status, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("errors", errors);
        response.put("data", null);
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Helper method to create paginated data structure matching the API requirements
     */
    private static <T> Map<String, Object> createPaginatedData(Page<T> page, String basePath) {
        Map<String, Object> data = new HashMap<>();
        data.put("current_page", page.getNumber() + 1); // Spring uses 0-based, API uses 1-based
        data.put("data", page.getContent());
        data.put("first_page_url", createPageUrl(basePath, 1));
        data.put("from", page.isEmpty() ? 0 : page.getNumber() * page.getSize() + 1);
        data.put("last_page", page.getTotalPages());
        data.put("last_page_url", createPageUrl(basePath, page.getTotalPages()));
        data.put("next_page_url", page.hasNext() ? createPageUrl(basePath, page.getNumber() + 2) : null);
        data.put("path", basePath);
        data.put("per_page", page.getSize());
        data.put("prev_page_url", page.hasPrevious() ? createPageUrl(basePath, page.getNumber()) : null);
        data.put("to", page.isEmpty() ? 0 : Math.min((page.getNumber() + 1) * page.getSize(), (int) page.getTotalElements()));
        data.put("total", page.getTotalElements());
        
        // Add links array for Laravel-style pagination
        Map<String, Object> links = new HashMap<>();
        links.put("first", createPageUrl(basePath, 1));
        links.put("last", createPageUrl(basePath, page.getTotalPages()));
        if (page.hasPrevious()) {
            links.put("prev", createPageUrl(basePath, page.getNumber()));
        }
        if (page.hasNext()) {
            links.put("next", createPageUrl(basePath, page.getNumber() + 2));
        }
        data.put("links", links);
        
        return data;
    }
    
    /**
     * Helper method to create page URLs
     */
    private static String createPageUrl(String basePath, int pageNumber) {
        return String.format("%s?page=%d", basePath, pageNumber);
    }
}
