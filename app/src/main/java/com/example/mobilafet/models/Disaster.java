package com.example.mobilafet.models;

/**
 * Domain model for a disaster event. Populated later from API / Firebase — no persistence in this phase.
 */
public class Disaster {

    private String type;
    private double magnitude;
    private double latitude;
    private double longitude;
    private long timestamp;
    private String locationName;
    private String description;

    public Disaster() {
    }

    public Disaster(
            String type,
            double magnitude,
            double latitude,
            double longitude,
            long timestamp,
            String locationName,
            String description
    ) {
        this.type = type;
        this.magnitude = magnitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.locationName = locationName;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
