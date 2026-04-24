package com.example.mobilafet.models;

/**
 * Basic user profile fields for future auth / settings. Not loaded from storage in this phase.
 */
public class UserProfile {

    private String username;
    private String region;
    private String emergencyPreference;

    public UserProfile() {
    }

    public UserProfile(String username, String region, String emergencyPreference) {
        this.username = username;
        this.region = region;
        this.emergencyPreference = emergencyPreference;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEmergencyPreference() {
        return emergencyPreference;
    }

    public void setEmergencyPreference(String emergencyPreference) {
        this.emergencyPreference = emergencyPreference;
    }
}
