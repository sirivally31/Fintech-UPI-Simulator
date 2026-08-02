package com.example.demo.dto;

import com.example.demo.entity.AuditAction;
import com.example.demo.entity.AuditLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Audit Log Details Response")
public class AuditLogResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Primary Key ID", example = "101")
    private Long id;

    @Schema(description = "Unique Event UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
    private String eventId;

    @Schema(description = "Username executing operation", example = "john_doe")
    private String username;

    @Schema(description = "User ID", example = "15")
    private Long userId;

    @Schema(description = "Audit Action Enum", example = "TRANSFER")
    private AuditAction action;

    @Schema(description = "System Module", example = "MONEY_TRANSFER")
    private String module;

    @Schema(description = "Target Entity Name", example = "Transaction")
    private String entityName;

    @Schema(description = "Target Entity Identifier", example = "TXN12345678")
    private String entityId;

    @Schema(description = "HTTP Request Method", example = "POST")
    private String requestMethod;

    @Schema(description = "HTTP Request URI", example = "/api/v1/transfers")
    private String requestUri;

    @Schema(description = "Client IP Address", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "User Agent", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "Request Body Payload Snippet")
    private String requestBody;

    @Schema(description = "Response Body Payload Snippet")
    private String responseBody;

    @Schema(description = "HTTP Status Code", example = "200")
    private Integer httpStatus;

    @Schema(description = "Operation Success Flag", example = "true")
    private Boolean success;

    @Schema(description = "Execution Duration in milliseconds", example = "42")
    private Long executionTime;

    @Schema(description = "Timestamp when event occurred")
    private LocalDateTime timestamp;

    public AuditLogResponse() {
    }

    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        AuditLogResponse dto = new AuditLogResponse();
        dto.setId(auditLog.getId());
        dto.setEventId(auditLog.getEventId());
        dto.setUsername(auditLog.getUsername());
        dto.setUserId(auditLog.getUserId());
        dto.setAction(auditLog.getAction());
        dto.setModule(auditLog.getModule());
        dto.setEntityName(auditLog.getEntityName());
        dto.setEntityId(auditLog.getEntityId());
        dto.setRequestMethod(auditLog.getRequestMethod());
        dto.setRequestUri(auditLog.getRequestUri());
        dto.setClientIp(auditLog.getClientIp());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setRequestBody(auditLog.getRequestBody());
        dto.setResponseBody(auditLog.getResponseBody());
        dto.setHttpStatus(auditLog.getHttpStatus());
        dto.setSuccess(auditLog.getSuccess());
        dto.setExecutionTime(auditLog.getExecutionTime());
        dto.setTimestamp(auditLog.getTimestamp());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
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
}
