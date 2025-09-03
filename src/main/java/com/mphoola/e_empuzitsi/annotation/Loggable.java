package com.mphoola.e_empuzitsi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark entities for activity logging
 * This provides fine-grained control over which entities should be logged
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    
    /**
     * The log name to use for this entity
     * Defaults to "default"
     */
    String logName() default "default";
    
    /**
     * Whether to log create events
     */
    boolean logCreate() default true;
    
    /**
     * Whether to log update events
     */
    boolean logUpdate() default true;
    
    /**
     * Whether to log delete events
     */
    boolean logDelete() default true;
    
    /**
     * Fields to exclude from logging
     */
    String[] excludeFields() default {};
    
    /**
     * Fields to include in logging (if specified, only these fields will be logged)
     */
    String[] includeFields() default {};
}
