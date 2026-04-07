package com.example.ticketreservationapp.models;

// ---
// Represents a ticket document in firestore Tickets collection
// Each reservation may have one or more associated tickets created by the makeReservation cloud function
// ---

public class Ticket {
    private String ticketID;
    private String reservationID;
    private String eventID;
    private String customerID;
    private String status;  // "valid", "canceled"
    private long issuedAt;

    // Empty constructor required for firestore deserialization
    public Ticket() {}

    public Ticket(String ticketID, String reservationID, String eventID, String customerID, String status, long issuedAt) {
        this.ticketID = ticketID;
        this.reservationID = reservationID;
        this.eventID = eventID;
        this.customerID = customerID;
        this.status = status;
        this.issuedAt = issuedAt;
    }

    // Getters
    public String getTicketID() { return ticketID; }
    public String getReservationID() { return reservationID; }
    public String getEventID() { return eventID; }
    public String getCustomerID() { return customerID; }
    public String getStatus() { return status; }
    public long getIssuedAt() { return issuedAt; }

    // Setters (needed for firestore)
    public void setTicketID(String ticketID) { this.ticketID = ticketID; }
    public void setReservationID(String reservationID) { this.reservationID = reservationID; }
    public void setEventID(String eventID) { this.eventID = eventID; }
    public void setCustomerID(String customerID) { this.customerID = customerID; }
    public void setStatus(String status) { this.status = status; }
    public void setIssuedAt(long issuedAt) { this.issuedAt = issuedAt; }

    public boolean isValid() {
        return "valid".equals(status);
    }

    public boolean isCanceled() {
        return "canceled".equals(status);
    }
}
