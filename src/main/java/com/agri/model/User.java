package com.agri.model;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String email;
    private String password;
    private FarmerProfile profile;
    private List<CropPlot> plots;

    public User() {}

    public User(String userId, String username, String email,String password) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password=password;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public FarmerProfile getProfile() { return profile; }
    public void setProfile(FarmerProfile profile) { this.profile = profile; }
}
