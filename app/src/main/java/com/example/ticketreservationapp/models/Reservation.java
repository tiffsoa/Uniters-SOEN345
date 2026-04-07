package com.example.ticketreservationapp.models;

import java.util.List;

// ---
// Represents a reservation document in firestore Reservations collection
// Created by the makeReservation cloud function
// ---

public class Reservation {
    private String reservationID;
    private String eventID;
    private String customerID;
    private int quantity;
    private String status;  // "confirmed", "canceled"
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private long createdAt;
    private List<String> ticketIDs;

    // Empty constructor required for firestore deserialization
    public Reservation() {}

    public Reservation(String reservationID, String eventID, String customerID, int quantity, String status, String eventName, String eventDate, String eventLocation, long createdAt, List<String> ticketIDs) {
        this.reservationID = reservationID;
        this.eventID = eventID;
        this.customerID = customerID;
        this.quantity = quantity;
        this.status = status;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.createdAt = createdAt;
        this.ticketIDs = ticketIDs;
    }

    // Getters
    public String getReservationID() { return reservationID; }
    public String getEventID() { return eventID; }
    public String getCustomerID() { return customerID; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
    public String getEventName() { return eventName; }
    public String getEventDate() { return eventDate; }
    public String getEventLocation() { return eventLocation; }
    public long getCreatedAt() { return createdAt; }
    public List<String> getTicketIDs() { return ticketIDs; }

    // Setters (needed for firestore)
    public void setReservationID(String reservationID) { this.reservationID = reservationID; }
    public void setEventID(String eventID) { this.eventID = eventID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setStatus(String status) { this.status = status; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setTicketIDs(List<String> ticketIDs) { this.ticketIDs = ticketIDs; }

    public boolean isConfirmed() {
        return "confirmed".equals(status);
    }

    public boolean isCanceled() {
        return "canceled".equals(status);
    }
}
