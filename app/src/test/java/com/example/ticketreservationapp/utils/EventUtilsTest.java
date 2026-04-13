package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.models.Event;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class EventUtilsTest {
    private Event createEvent() {
        return new Event(
                "1",
                "Concert",
                new Date(System.currentTimeMillis() + 100000),
                "Montreal",
                "Music",
                100
        );
    }

    @Test
    void validateEvent_valid_shouldPass() {
        assertDoesNotThrow(() -> EventUtils.validateEvent(createEvent()));
    }

    @Test
    void validateEvent_null_shouldThrow() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> EventUtils.validateEvent(null));

        assertEquals("Event cannot be null", e.getMessage());
    }

    @Test
    void cancelEvent_activeEvent_shouldMarkCancelled() {
        Event e = createEvent();
        EventUtils.cancelEvent(e);

        assertTrue(e.isCancelled());
    }
}