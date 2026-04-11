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
    private boolean isCancelled;

    public Event() {}

    public Event(String eventID, String name, Date date, String location, String category, int capacity) {
        this.eventID = eventID;
        setName(name);
        setDate(date);
        setLocation(location);
        setCategory(category);
        setCapacity(capacity);
        this.bookedSeats = 0; // for later when reservation is implemented
        this.isCancelled = false; // By default, events are not canceled
    }

    // Getters and setters
    public String getEventID() { return eventID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getDate() { return (Date) date.clone(); }
    public void setDate(Date date) {
        if (date == null || date.before(new Date())) {
            throw new IllegalArgumentException("Event date cannot be in the past");
        }
        this.date = date;
    }
    public String getLocation() { return location; }
    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Event location cannot be empty");
        }
        this.location = location;
    }
    public String getCategory() { return category; }
    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Event category cannot be empty");
        }
        this.category = category;
    }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Event capacity must be greater than zero");
        }
        this.capacity = capacity;
    }
    public int getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(int bookedSeats) {
        if (bookedSeats < 0 || bookedSeats > capacity) {
            throw new IllegalArgumentException("Booked seats cannot be negative or exceed the event capacity");
        }
        this.bookedSeats = bookedSeats;
    }
    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) {
        if (isCancelled) {
            throw new IllegalStateException("Cannot modify cancelled event");
        }
        isCancelled = cancelled;
    }
}