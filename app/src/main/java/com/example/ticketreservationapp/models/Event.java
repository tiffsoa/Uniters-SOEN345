package com.example.ticketreservationapp.models;

// Represents an event document in firestore Events collection

public class Event {
    private String eventID;
    private String name;
    private String date;    // ISO date string for firestore compatibility (e.g. "2026-06-15T19:00:00")
    private String location;
    private String category;
    private int capacity;
    private int bookedSeats;
    private String status;  // "active", "canceled"

    // Empty constructor required for firestore deserialization
    public Event() {}

    // Constructor
    public Event(String eventID, String name, String date, String location, String category, int capacity) {
        this.eventID = eventID;
        this.name = name;
        this.date = date;
        this.location = location;
        this.category = category;
        this.capacity = capacity;
        this.bookedSeats = 0;
        this.status = "active";
    }

    // Getters
    public String getEventID() { return eventID; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public int getCapacity() { return capacity; }
    public int getBookedSeats() { return bookedSeats; }
    public String getStatus() { return status; }

    // Setters (needed for firestore)
    public void setEventID(String eventID) { this.eventID = eventID; }
    public void setName(String name) { this.name = name; }
    public void setDate(String date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setBookedSeats(int bookedSeats) { this.bookedSeats = bookedSeats; }
    public void setStatus(String status) { this.status = status; }

    public int getRemainingCapacity() {
        return capacity - bookedSeats;
    }

    public void updateDetails(String name, String date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }

    public void cancel() {
        this.status = "canceled";
    }
}