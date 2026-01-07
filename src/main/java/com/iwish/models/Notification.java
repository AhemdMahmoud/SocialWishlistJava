package com.iwish.models;

import java.sql.Timestamp;

public class Notification {
    private int notificationId;
    private int userId;
    private String type;
    private String message;
    private Integer relatedId;
    private boolean read;
    private Timestamp notificationDate;

    public Notification() {
    }

    public Notification(int notificationId, int userId, String type, String message,
                        Integer relatedId, boolean read, Timestamp notificationDate) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.relatedId = relatedId;
        this.read = read;
        this.notificationDate = notificationDate;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Integer relatedId) {
        this.relatedId = relatedId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Timestamp getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(Timestamp notificationDate) {
        this.notificationDate = notificationDate;
    }
}