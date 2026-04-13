package com.example.ticketreservationapp.domain;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import com.example.ticketreservationapp.utils.ReservationUtils;

import java.util.UUID;

// Business logic for reservation and cancellation
public class ReservationService {

    public Reservation createReservation(String eventID, String customerID, int ticketCount, Event event) {
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                eventID,
                customerID,
                ticketCount
        );
        ReservationUtils.validateReservation(reservation, event);
        return reservation;
    }

    public void cancel(Reservation reservation) {
        ReservationUtils.cancelReservation(reservation);
    }
}
