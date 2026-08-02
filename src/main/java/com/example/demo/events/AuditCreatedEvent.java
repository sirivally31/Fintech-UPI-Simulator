package com.example.demo.events;

import com.example.demo.entity.AuditAction;
import com.example.demo.entity.AuditLog;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain Event representing the creation of an audit log entry.
 */
public class AuditCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String username;
    private Long userId;
    private AuditAction action;
    private String module;
    private String requestUri;
    private Boolean success;
    private Long executionTime;
    private LocalDateTime timestamp;
    private String correlationId;

    public AuditCreatedEvent() {
    }

    public static AuditCreatedEvent fromEntity(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        AuditCreatedEvent event = new AuditCreatedEvent();
        event.setEventId(auditLog.getEventId());
        event.setUsername(auditLog.getUsername());
        event.setUserId(auditLog.getUserId());
        event.setAction(auditLog.getAction());
        event.setModule(auditLog.getModule());
        event.setRequestUri(auditLog.getRequestUri());
        event.setSuccess(auditLog.getSuccess());
        event.setExecutionTime(auditLog.getExecutionTime());
        event.setTimestamp(auditLog.getTimestamp());
        event.setCorrelationId(auditLog.getEventId());
        return event;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId != null ? correlationId : eventId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
