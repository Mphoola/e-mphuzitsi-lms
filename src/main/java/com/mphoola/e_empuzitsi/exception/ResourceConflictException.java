package com.mphoola.e_empuzitsi.exception;

public class ResourceConflictException extends RuntimeException {
    
    public ResourceConflictException(String message) {
        super(message);
    }
    
    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
