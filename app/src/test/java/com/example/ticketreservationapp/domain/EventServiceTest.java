package com.example.ticketreservationapp.domain;

import com.example.ticketreservationapp.models.Event;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EventServiceTest {
    EventService service = new EventService();

    @Test
    void createEvent_success() throws Exception {
        Event e = service.createEvent(
                "Concert",
                "laval",
                "Music",
                "450",
                "10/12/2026"
        );

        assertNotNull(e);
        assertEquals("Concert", e.getName());
        assertEquals(450, e.getCapacity());
    }

    @Test
    void createEvent_invalidCapacity_shouldThrow() {
        assertThrows(Exception.class, () -> service.createEvent(
                "A",
                "B",
                "C",
                "abc",
                "10/12/2026"
        ));
    }

    @Test
    void updateEvent_shouldPreserveState() throws Exception {
        Event old = service.createEvent(
                "A",
                "B",
                "C",
                "50",
                "10/12/2026"
        );
        old.setCancelled(true);

        Event updated = service.updateEvent(
                old,
                "A2",
                "B",
                "C",
                "450",
                "10/12/2026"
        );

        assertTrue(updated.isCancelled());
        assertEquals(450, updated.getCapacity());
    }

    @Test
    void cancel_shouldMarkCancelled() throws Exception {
        Event e = service.createEvent(
                "A",
                "B",
                "C",
                "10",
                "10/12/2026"
        );

        service.cancel(e);
        assertTrue(e.isCancelled());
    }
}