package com.example.ticketreservationapp;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import com.example.ticketreservationapp.models.Event;

public class EventRepositoryTest {

    @Test
    public void testEventCreation() {
        Event event = new Event("Test Event", 10, 5);
        assertEquals("Test Event", event.getName());
        assertEquals(10, event.getCapacity());
        assertEquals(5, event.getBookedSeats());
    }

    @Test
    public void testAvailableSeats() {
        Event event = new Event("Test Event", 10, 5);
        assertTrue(event.getCapacity() > event.getBookedSeats());
    }

    @Test
    public void testEventListFiltering() {
        List<Event> events = new ArrayList<>();

        events.add(new Event("Event1", 10, 10)); // full
        events.add(new Event("Event2", 10, 5));  // available

        List<Event> available = new ArrayList<>();

        for (Event e : events) {
            if (e.getCapacity() > e.getBookedSeats()) {
                available.add(e);
            }
        }

        assertEquals(1, available.size());
        assertEquals("Event2", available.get(0).getName());
    }
}
