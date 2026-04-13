package com.example.ticketreservationapp.models;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    @Test
    void constructor_allFields_shouldInitializeDefaults() {
        Date date = new Date();
        Event event = new Event("1", "Concert", date, "Toronto", "Music", 100);

        assertEquals("1", event.getEventID());
        assertEquals("Concert", event.getName());
        assertEquals(date, event.getDate());
        assertEquals("Toronto", event.getLocation());
        assertEquals("Music", event.getCategory());
        assertEquals(100, event.getCapacity());
        assertFalse(event.isCancelled());
    }

    @Test
    void setters_allFields_shouldUpdateFields() {
        Event event = new Event();
        Date date = new Date();

        event.setName("New Name");
        event.setLocation("NYC");
        event.setCategory("Sports");
        event.setCapacity(200);
        event.setCancelled(true);
        event.setDate(date);

        assertEquals("New Name", event.getName());
        assertEquals("NYC", event.getLocation());
        assertEquals("Sports", event.getCategory());
        assertEquals(200, event.getCapacity());
        assertTrue(event.isCancelled());
        assertEquals(date, event.getDate());
    }

    @Test
    void getDate_shouldReturnDefensiveCopy() {
        Date original = new Date();
        Event event = new Event("1", "Test", original, "Loc", "Cat", 10);
        Date returned = event.getDate();
        returned.setTime(0);

        assertNotEquals(returned.getTime(), event.getDate().getTime());
    }

    @Test
    void setDate_shouldUpdateDate() {
        Event event = new Event();
        Date d1 = new Date();
        event.setDate(d1);

        assertEquals(d1, event.getDate());
    }

    @Test
    void isCancelled_newEvent_shouldDefaultToFalse() {
        Event event = new Event("1", "Test", new Date(), "Loc", "Cat", 50);

        assertFalse(event.isCancelled());
    }
}