package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationUtilsTest {

    private Event createEvent(int capacity, int booked, boolean cancelled) {
        Event e = new Event("e1", "Concert", new Date(), "Montreal", "Music", capacity);
        e.setBookedSeats(booked);
        e.setCancelled(cancelled);
        return e;
    }

    private Reservation createReservation(String eventID, String customerID, int tickets) {
        return new Reservation("r1", eventID, customerID, tickets);
    }

    // validateReservation:

    @Test
    void validateReservation_valid_shouldPass() {
        Event event = createEvent(100, 10, false);
        Reservation res = createReservation("e1", "c1", 5);

        assertDoesNotThrow(() -> ReservationUtils.validateReservation(res, event));
    }

    @Test
    void validateReservation_nullReservation_shouldThrow() {
        Event event = createEvent(100, 0, false);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(null, event));
        assertEquals("Reservation cannot be null", e.getMessage());
    }

    @Test
    void validateReservation_nullEvent_shouldThrow() {
        Reservation res = createReservation("e1", "c1", 2);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, null));
        assertEquals("Event cannot be null", e.getMessage());
    }

    @Test
    void validateReservation_emptyEventID_shouldThrow() {
        Event event = createEvent(100, 0, false);
        Reservation res = createReservation("", "c1", 2);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Event ID is required", e.getMessage());
    }

    @Test
    void validateReservation_emptyCustomerID_shouldThrow() {
        Event event = createEvent(100, 0, false);
        Reservation res = createReservation("e1", "", 2);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Customer ID is required", e.getMessage());
    }

    @Test
    void validateReservation_nullCustomerID_shouldThrow() {
        Event event = createEvent(100, 0, false);
        Reservation res = createReservation("e1", null, 2);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Customer ID is required", e.getMessage());
    }

    @Test
    void validateReservation_zeroTickets_shouldThrow() {
        Event event = createEvent(100, 0, false);
        Reservation res = createReservation("e1", "c1", 0);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Ticket count must be > 0", e.getMessage());
    }

    @Test
    void validateReservation_negativeTickets_shouldThrow() {
        Event event = createEvent(100, 0, false);
        Reservation res = createReservation("e1", "c1", -3);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Ticket count must be > 0", e.getMessage());
    }

    @Test
    void validateReservation_cancelledEvent_shouldThrow() {
        Event event = createEvent(100, 0, true);
        Reservation res = createReservation("e1", "c1", 2);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Cannot reserve a cancelled event", e.getMessage());
    }

    @Test
    void validateReservation_exceedsCapacity_shouldThrow() {
        Event event = createEvent(10, 8, false);
        Reservation res = createReservation("e1", "c1", 5);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Not enough seats available (remaining: 2)", e.getMessage());
    }

    @Test
    void validateReservation_exactCapacity_shouldPass() {
        Event event = createEvent(10, 5, false);
        Reservation res = createReservation("e1", "c1", 5);

        assertDoesNotThrow(() -> ReservationUtils.validateReservation(res, event));
    }

    @Test
    void validateReservation_soldOut_shouldThrow() {
        Event event = createEvent(10, 10, false);
        Reservation res = createReservation("e1", "c1", 1);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.validateReservation(res, event));
        assertEquals("Not enough seats available (remaining: 0)", e.getMessage());
    }

    // cancelReservation:

    @Test
    void cancelReservation_shouldMarkCancelled() {
        Reservation res = createReservation("e1", "c1", 2);

        ReservationUtils.cancelReservation(res);

        assertTrue(res.isCancelled());
    }

    @Test
    void cancelReservation_null_shouldThrow() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.cancelReservation(null));
        assertEquals("Reservation cannot be null", e.getMessage());
    }

    @Test
    void cancelReservation_alreadyCancelled_shouldThrow() {
        Reservation res = createReservation("e1", "c1", 2);
        res.setCancelled(true);

        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ReservationUtils.cancelReservation(res));
        assertEquals("Reservation is already cancelled", e.getMessage());
    }

    // filterByCustomer:

    @Test
    void filterByCustomer_shouldReturnOnlyMatching() {
        Reservation r1 = createReservation("e1", "c1", 1);
        Reservation r2 = createReservation("e2", "c2", 2);
        Reservation r3 = createReservation("e3", "c1", 3);

        List<Reservation> result = ReservationUtils.filterByCustomer(
                Arrays.asList(r1, r2, r3), "c1");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> "c1".equals(r.getCustomerID())));
    }

    @Test
    void filterByCustomer_noMatch_shouldReturnEmpty() {
        Reservation r1 = createReservation("e1", "c1", 1);

        List<Reservation> result = ReservationUtils.filterByCustomer(
                List.of(r1), "c99");

        assertTrue(result.isEmpty());
    }

    // filterActive:

    @Test
    void filterActive_shouldExcludeCancelled() {
        Reservation r1 = createReservation("e1", "c1", 1);
        Reservation r2 = createReservation("e2", "c1", 2);
        r2.setCancelled(true);
        Reservation r3 = createReservation("e3", "c1", 3);

        List<Reservation> result = ReservationUtils.filterActive(
                Arrays.asList(r1, r2, r3));

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(Reservation::isCancelled));
    }

    @Test
    void filterActive_allCancelled_shouldReturnEmpty() {
        Reservation r1 = createReservation("e1", "c1", 1);
        r1.setCancelled(true);

        List<Reservation> result = ReservationUtils.filterActive(List.of(r1));

        assertTrue(result.isEmpty());
    }

    @Test
    void filterActive_noneActive_emptyList() {
        List<Reservation> result = ReservationUtils.filterActive(List.of());

        assertTrue(result.isEmpty());
    }
}
