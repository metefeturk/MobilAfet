package com.example.mobilafet.models;

public class Report {
    public String title;
    public String description;
    public String type;
    public String city;
    public String userId;
    public String status;
    public double latitude;
    public double longitude;
    public long createdAt;

    public Report() { } // Firestore için boş yapıcı metod (constructor) şarttır.
}