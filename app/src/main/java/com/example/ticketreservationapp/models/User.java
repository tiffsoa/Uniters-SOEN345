package com.example.ticketreservationapp.models;

public abstract class User {
    private String userID;
    private String email;
    private String phoneNumber;

    public User(String userID, String email, String phoneNumber) {
        this.userID = userID;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getUserID() { return userID; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }

    // Abstract/default login method
    public boolean login() {
        // Firebase Auth logic
        return true;
    }
}