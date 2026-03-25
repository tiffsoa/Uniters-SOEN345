package com.example.ticketreservationapp.models;

public class Administrator extends User {

    public Administrator(String userID, String email, String phoneNumber) {
        super(userID, email, phoneNumber);
    }

    public void addEvent(Event event) {
        // Logic to add to EventsCatalog
    }

    public void editEvent(Event event) {
        // Logic to update EventsCatalog
    }

    public void cancelEvent(Event event) {
        // Logic to remove/flag in EventsCatalog
    }
}