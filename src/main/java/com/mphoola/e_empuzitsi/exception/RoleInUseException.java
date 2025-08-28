package com.mphoola.e_empuzitsi.exception;

public class RoleInUseException extends RuntimeException {
    
    public RoleInUseException(String message) {
        super(message);
    }
    
    public RoleInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
