package com.library.model;

import java.sql.Timestamp;

public class ActivityLog {
    private int logId;
    private String adminUsername;
    private String actionType;
    private String description;
    private Timestamp createdAt;

    public ActivityLog() {}

    public ActivityLog(int logId, String adminUsername, String actionType, String description, Timestamp createdAt) {
        this.logId = logId;
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.description = description;
        this.createdAt = createdAt;
    }

    public ActivityLog(String adminUsername, String actionType, String description) {
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.description = description;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
