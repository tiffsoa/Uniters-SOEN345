package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.models.Event;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.Arrays;
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

    private Event createEventWith(String location, String category) {
        return new Event(
                "2",
                "Test Event",
                new Date(System.currentTimeMillis() + 100000),
                location,
                category,
                50
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

    // --- filter() tests ---

    @Test
    void filter_nullFilters_shouldReturnAllEvents() {
        List<Event> events = Arrays.asList(createEvent(), createEventWith("Toronto", "Sports"));
        List<Event> result = EventUtils.filter(events, null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void filter_exactLocation_shouldReturnMatch() {
        List<Event> events = Arrays.asList(
                createEventWith("Civic Center", "Music"),
                createEventWith("Bell Arena", "Sports")
        );
        List<Event> result = EventUtils.filter(events, null, "Civic Center", null);

        assertEquals(1, result.size());
        assertEquals("Civic Center", result.get(0).getLocation());
    }

    @Test
    void filter_partialLocation_shouldReturnContainingMatches() {
        List<Event> events = Arrays.asList(
                createEventWith("Civic Center", "Music"),
                createEventWith("Bell Arena", "Sports")
        );
        List<Event> result = EventUtils.filter(events, null, "center", null);

        assertEquals(1, result.size());
        assertEquals("Civic Center", result.get(0).getLocation());
    }

    @Test
    void filter_partialCategory_shouldReturnContainingMatches() {
        List<Event> events = Arrays.asList(
                createEventWith("Montreal", "Music Festival"),
                createEventWith("Toronto", "Sports")
        );
        List<Event> result = EventUtils.filter(events, null, null, "mus");

        assertEquals(1, result.size());
        assertEquals("Music Festival", result.get(0).getCategory());
    }

    @Test
    void filter_caseInsensitiveLocation_shouldMatch() {
        List<Event> events = Arrays.asList(createEventWith("Montreal", "Music"));
        List<Event> result = EventUtils.filter(events, null, "MONTREAL", null);

        assertEquals(1, result.size());
    }

    @Test
    void filter_noMatch_shouldReturnEmptyList() {
        List<Event> events = Arrays.asList(createEventWith("Montreal", "Music"));
        List<Event> result = EventUtils.filter(events, null, "Vancouver", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void filter_dateMatch_shouldReturnEventsOnSameDay() {
        Date future = new Date(System.currentTimeMillis() + 100000);
        Event e = new Event("3", "Show", future, "Montreal", "Music", 100);
        List<Event> events = Arrays.asList(e);
        List<Event> result = EventUtils.filter(events, future, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void filter_dateMismatch_shouldReturnEmptyList() {
        Date tomorrow = new Date(System.currentTimeMillis() + 86400000L);
        Date dayAfter = new Date(System.currentTimeMillis() + 172800000L);
        Event e = new Event("3", "Show", tomorrow, "Montreal", "Music", 100);
        List<Event> result = EventUtils.filter(Arrays.asList(e), dayAfter, null, null);

        assertTrue(result.isEmpty());
    }
}