package com.example.ticketreservationapp.domain;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationServiceTest {

    ReservationService service = new ReservationService();

    private Event createEvent(int capacity, int booked, boolean cancelled) {
        Event e = new Event("e1", "Concert", new Date(), "Montreal", "Music", capacity);
        e.setBookedSeats(booked);
        e.setCancelled(cancelled);
        return e;
    }

    // createReservation:

    @Test
    void createReservation_success() {
        Event event = createEvent(100, 10, false);
        Reservation r = service.createReservation("e1", "c1", 5, event);

        assertNotNull(r);
        assertNotNull(r.getReservationID());
        assertEquals("e1", r.getEventID());
        assertEquals("c1", r.getCustomerID());
        assertEquals(5, r.getTicketCount());
        assertFalse(r.isCancelled());
    }

    @Test
    void createReservation_generatesUniqueIDs() {
        Event event = createEvent(100, 0, false);
        Reservation r1 = service.createReservation("e1", "c1", 1, event);
        Reservation r2 = service.createReservation("e1", "c1", 1, event);

        assertNotEquals(r1.getReservationID(), r2.getReservationID());
    }

    @Test
    void createReservation_exceedsCapacity_shouldThrow() {
        Event event = createEvent(10, 9, false);

        assertThrows(IllegalArgumentException.class,
                () -> service.createReservation("e1", "c1", 5, event));
    }

    @Test
    void createReservation_cancelledEvent_shouldThrow() {
        Event event = createEvent(100, 0, true);

        assertThrows(IllegalArgumentException.class,
                () -> service.createReservation("e1", "c1", 1, event));
    }

    @Test
    void createReservation_zeroTickets_shouldThrow() {
        Event event = createEvent(100, 0, false);

        assertThrows(IllegalArgumentException.class,
                () -> service.createReservation("e1", "c1", 0, event));
    }

    @Test
    void createReservation_exactRemaining_shouldPass() {
        Event event = createEvent(10, 7, false);
        Reservation r = service.createReservation("e1", "c1", 3, event);

        assertNotNull(r);
        assertEquals(3, r.getTicketCount());
    }

    @Test
    void createReservation_emptyCustomerID_shouldThrow() {
        Event event = createEvent(100, 0, false);

        assertThrows(IllegalArgumentException.class,
                () -> service.createReservation("e1", "", 1, event));
    }

    // cancel:

    @Test
    void cancel_shouldMarkCancelled() {
        Event event = createEvent(100, 10, false);
        Reservation r = service.createReservation("e1", "c1", 2, event);

        service.cancel(r);

        assertTrue(r.isCancelled());
    }

    @Test
    void cancel_alreadyCancelled_shouldThrow() {
        Event event = createEvent(100, 0, false);
        Reservation r = service.createReservation("e1", "c1", 1, event);
        r.setCancelled(true);

        assertThrows(IllegalArgumentException.class, () -> service.cancel(r));
    }

    @Test
    void cancel_null_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> service.cancel(null));
    }
}
