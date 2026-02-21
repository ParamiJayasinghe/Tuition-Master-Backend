package com.tuition.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private String action;
    private String actorUsername;
    private String actorRole;
    private String targetType;
    private String targetId;
    private String details;
    private String timestamp;

    // Constructors
    public AuditLogDTO() {}

    public AuditLogDTO(Long id, String action, String actorUsername, String actorRole, String targetType, String targetId, String details, String timestamp) {
        this.id = id;
        this.action = action;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
