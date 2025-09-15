package com.mphoola.e_empuzitsi.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark endpoints that allow access without email verification.
 * By default, all endpoints require email verification (secure by default).
 * Use this annotation on methods or classes to explicitly allow unverified users.
 * 
 * Example usage:
 * <pre>
 * @PostMapping("/auth/login")
 * @AllowUnverifiedEmail
 * public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
 *     // Login doesn't require email verification
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowUnverifiedEmail {
}