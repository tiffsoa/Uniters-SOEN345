package com.example.ticketreservationapp.models;

import com.google.firebase.Timestamp;

// ---
// Represents a reservation document in firestore Reservations collection
// Created by the makeReservation cloud function
// Event details (name, date, location) are fetched separately using eventID
// Fields: reservationID, eventID, customerID, ticketCount, isCancelled, createdAt (Timestamp)
// ---

public class Reservation {
    private String reservationID;
    private String eventID;
    private String customerID;
    private int ticketCount;
    private boolean isCancelled;
    private Timestamp createdAt;

    // Empty constructor required for firestore deserialization
    public Reservation() {}

    public Reservation(String reservationID, String eventID, String customerID,
                       int ticketCount, boolean isCancelled, Timestamp createdAt) {
        this.reservationID = reservationID;
        this.eventID = eventID;
        this.customerID = customerID;
        this.ticketCount = ticketCount;
        this.isCancelled = isCancelled;
        this.createdAt = createdAt;
    }

    // Getters
    public String getReservationID() { return reservationID; }
    public String getEventID() { return eventID; }
    public String getCustomerID() { return customerID; }
    public int getTicketCount() { return ticketCount; }
    public boolean isIsCancelled() { return isCancelled; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters (needed for firestore deserialization)
    public void setReservationID(String reservationID) { this.reservationID = reservationID; }
    public void setEventID(String eventID) { this.eventID = eventID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }
    public void setTicketCount(int ticketCount) { this.ticketCount = ticketCount; }
    public void setIsCancelled(boolean isCancelled) { this.isCancelled = isCancelled; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

