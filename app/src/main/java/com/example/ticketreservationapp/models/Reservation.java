package com.example.ticketreservationapp.models;

import java.util.Date;

public class Reservation {

    private String reservationID;
    private String eventID;
    private String customerID;
    private int ticketCount;
    private boolean isCancelled;
    private Date createdAt;

    public Reservation() {}

    public Reservation(String reservationID, String eventID, String customerID,
                       int ticketCount) {
        this.reservationID = reservationID;
        this.eventID = eventID;
        this.customerID = customerID;
        this.ticketCount = ticketCount;
        this.isCancelled = false;
        this.createdAt = new Date();
    }

    // Getters and setters
    public String getReservationID() { return reservationID; }

    public String getEventID() { return eventID; }

    public String getCustomerID() { return customerID; }

    public int getTicketCount() { return ticketCount; }
    public void setTicketCount(int ticketCount) { this.ticketCount = ticketCount; }

    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { this.isCancelled = cancelled; }

    public Date getCreatedAt() { return createdAt != null ? (Date) createdAt.clone() : null; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
