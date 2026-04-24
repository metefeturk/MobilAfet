package com.example.mobilafet.models;

public class ReportNotificationRequest {
    public String reportId, title, description, type, city, userId;

    public ReportNotificationRequest(String reportId, String title, String description, String type, String city, String userId) {
        this.reportId = reportId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.city = city;
        this.userId = userId;
    }
}