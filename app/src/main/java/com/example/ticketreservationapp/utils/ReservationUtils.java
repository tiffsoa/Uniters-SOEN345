package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;

import java.util.List;
import java.util.stream.Collectors;

public class ReservationUtils {

    public static void validateReservation(Reservation reservation, Event event) {
        if (reservation == null)
            throw new IllegalArgumentException("Reservation cannot be null");

        if (event == null)
            throw new IllegalArgumentException("Event cannot be null");

        if (reservation.getEventID() == null || reservation.getEventID().trim().isEmpty())
            throw new IllegalArgumentException("Event ID is required");

        if (reservation.getCustomerID() == null || reservation.getCustomerID().trim().isEmpty())
            throw new IllegalArgumentException("Customer ID is required");

        if (reservation.getTicketCount() <= 0)
            throw new IllegalArgumentException("Ticket count must be > 0");

        if (event.isCancelled())
            throw new IllegalArgumentException("Cannot reserve a cancelled event");

        int remaining = event.getCapacity() - event.getBookedSeats();
        if (reservation.getTicketCount() > remaining)
            throw new IllegalArgumentException("Not enough seats available (remaining: " + remaining + ")");
    }

    public static void cancelReservation(Reservation reservation) {
        if (reservation == null)
            throw new IllegalArgumentException("Reservation cannot be null");

        if (reservation.isCancelled())
            throw new IllegalArgumentException("Reservation is already cancelled");

        reservation.setCancelled(true);
    }

    public static List<Reservation> filterByCustomer(List<Reservation> reservations, String customerID) {
        return reservations.stream()
                .filter(r -> customerID.equals(r.getCustomerID()))
                .collect(Collectors.toList());
    }

    public static List<Reservation> filterActive(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> !r.isCancelled())
                .collect(Collectors.toList());
    }
}
