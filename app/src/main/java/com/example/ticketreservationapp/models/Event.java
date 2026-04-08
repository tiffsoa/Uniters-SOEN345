package com.example.ticketreservationapp.models;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

// ---
// Represents an event document in firestore Events collection
// Fields: eventID, name, date (Timestamp), location, category, maxCapacity, bookedSeats, isCancelled
// remainingCapacity is inferred: maxCapacity - bookedSeats
// ---

public class Event {
    private String eventID;
    private String name;
    private Timestamp date;
    private String location;
    private String category;
    private int maxCapacity;
    private int bookedSeats;
    private boolean isCancelled;

    // Empty constructor required for firestore deserialization
    public Event() {}

    public Event(String eventID, String name, Timestamp date, String location, String category,
                 int maxCapacity, int bookedSeats) {
        this.eventID = eventID;
        this.name = name;
        this.date = date;
        this.location = location;
        this.category = category;
        this.maxCapacity = maxCapacity;
        this.bookedSeats = bookedSeats;
        this.isCancelled = false;
    }

    // Getters
    public String getEventID() { return eventID; }
    public String getName() { return name; }
    public Timestamp getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public int getMaxCapacity() { return maxCapacity; }
    public int getBookedSeats() { return bookedSeats; }
    public int getRemainingCapacity() { return maxCapacity - bookedSeats; }  // inferred
    public boolean isIsCancelled() { return isCancelled; }

    // Setters (needed for firestore deserialization)
    public void setEventID(String eventID) { this.eventID = eventID; }
    public void setName(String name) { this.name = name; }
    public void setDate(Timestamp date) { this.date = date; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public void setBookedSeats(int bookedSeats) { this.bookedSeats = bookedSeats; }
    public void setIsCancelled(boolean isCancelled) { this.isCancelled = isCancelled; }

    // Returns a formatted date string for UI display
    public String getFormattedDate() {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(date.toDate());
    }
}