package com.example.ticketreservationapp.models;

import java.time.LocalDateTime;

public class Event {
    private String eventID;
    private String name;
    private LocalDateTime date;
    private String location;
    private String category;
    private int capacity;
    private int bookedSeats;

    // Constructor
    public Event(String eventID, String name, LocalDateTime date, String location, String category, int capacity) {
        this.eventID = eventID;
        this.name = name;
        this.date = date;
        this.location = location;
        this.category = category;
        this.capacity = capacity;
        this.bookedSeats = 0; // Always starts at 0 when created
    }

    public int getRemainingCapacity() {
        return capacity - bookedSeats;
    }

    public void updateDetails(String name, LocalDateTime date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }

    public void cancel() {
        //add the specific cancellation logic
    }

    public String getEventID() { return eventID; }
    public String getName() { return name; }
    public LocalDateTime getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
}