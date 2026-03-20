package com.example.ticketreservationapp.models;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {

    public Customer(String userID, String email, String phoneNumber) {
        super(userID, email, phoneNumber);
    }

    public boolean register(String email, String phone) {
        // Logic to register via Firebase Auth
        return true;
    }

    public List<Event> viewEvents() {
        // Will fetch from EventsCatalog later
        return new ArrayList<>();
    }

    public boolean cancelReservation(String reservationID) {
        // Logic to interact with BookingSystem
        return true;
    }
}