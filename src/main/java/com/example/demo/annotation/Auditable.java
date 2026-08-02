package com.example.demo.annotation;

import com.example.demo.entity.AuditAction;

import java.lang.annotation.*;

/**
 * Annotation to explicitly mark methods for enterprise audit logging.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    AuditAction action() default AuditAction.SYSTEM;

    String module() default "GENERAL";

    String entityName() default "";
}
