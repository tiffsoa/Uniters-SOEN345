package com.example.ticketreservationapp.models;

import java.util.Date;

public class Event {
    private String eventID;
    private String name;
    private Date date;
    private String location;
    private String category;
    private int capacity;
    private int bookedSeats;
    private boolean isCancelled;  // to track cancellation status

    // Constructor
    public Event() {}

    public Event(String eventID, String name, Date date, String location, String category, int capacity) {
        this.eventID = eventID;
        this.name = name;
        this.date = date;
        this.location = location;
        this.category = category;
        this.capacity = capacity;
        this.bookedSeats = 0; // for later when reservation is implemented
        this.isCancelled = false; // By default, events are not canceled
    }

    // Getters and setters
    public String getEventID() { return eventID; }
    public void setEventID(String eventID) { this.eventID = eventID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(int bookedSeats) { this.bookedSeats = bookedSeats; }

    public int getRemainingCapacity() { return capacity - bookedSeats; }

    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { isCancelled = cancelled; }

    // Cancel method that marks the event as canceled
    public void cancel() {
        this.isCancelled = true;
    }

    public void updateDetails(String name, Date date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }
}