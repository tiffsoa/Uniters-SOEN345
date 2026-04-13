package com.example.ticketreservationapp.models;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    @Test
    void constructor_shouldInitializeFields() {
        Reservation r = new Reservation("r1", "e1", "c1", 3);

        assertEquals("r1", r.getReservationID());
        assertEquals("e1", r.getEventID());
        assertEquals("c1", r.getCustomerID());
        assertEquals(3, r.getTicketCount());
        assertFalse(r.isCancelled());
        assertNotNull(r.getCreatedAt());
    }

    @Test
    void noArgConstructor_shouldCreateInstance() {
        Reservation r = new Reservation();

        assertNull(r.getReservationID());
        assertNull(r.getEventID());
        assertNull(r.getCustomerID());
        assertEquals(0, r.getTicketCount());
        assertFalse(r.isCancelled());
    }

    @Test
    void setTicketCount_shouldUpdate() {
        Reservation r = new Reservation("r1", "e1", "c1", 2);
        r.setTicketCount(5);

        assertEquals(5, r.getTicketCount());
    }

    @Test
    void setCancelled_shouldUpdate() {
        Reservation r = new Reservation("r1", "e1", "c1", 1);
        r.setCancelled(true);

        assertTrue(r.isCancelled());
    }

    @Test
    void getCreatedAt_shouldReturnDefensiveCopy() {
        Reservation r = new Reservation("r1", "e1", "c1", 1);
        Date returned = r.getCreatedAt();
        returned.setTime(0);

        assertNotEquals(0, r.getCreatedAt().getTime());
    }

    @Test
    void setCreatedAt_shouldUpdateDate() {
        Reservation r = new Reservation();
        Date date = new Date();
        r.setCreatedAt(date);

        assertEquals(date, r.getCreatedAt());
    }

    @Test
    void getCreatedAt_null_shouldReturnNull() {
        Reservation r = new Reservation();

        assertNull(r.getCreatedAt());
    }

    @Test
    void cancelledDefaultFalse_afterConstruction() {
        Reservation r = new Reservation("r1", "e1", "c1", 4);

        assertFalse(r.isCancelled());
    }
}
