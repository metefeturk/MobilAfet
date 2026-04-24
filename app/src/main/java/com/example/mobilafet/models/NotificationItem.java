package com.example.mobilafet.models;

/**
 * In-app notification / alert row. Filled when push or API layer is added.
 */
public class NotificationItem {

    private String title;
    private String message;
    private long timestamp;
    private String type;
    private boolean read;

    public NotificationItem() {
    }

    public NotificationItem(String title, String message, long timestamp, String type, boolean read) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.read = read;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
