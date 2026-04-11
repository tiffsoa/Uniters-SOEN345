package com.example.ticketreservationapp.domain;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.utils.DateParser;
import com.example.ticketreservationapp.utils.EventUtils;

import java.util.Date;
import java.util.UUID;
//business logic (for the 3 admin features)
public class EventService {
    public Event createEvent(String name, String location, String category, String capacityStr, String dateStr) throws Exception {
        int capacity = Integer.parseInt(capacityStr);
        Date date = DateParser.parse(dateStr);
        Event event = new Event(
                UUID.randomUUID().toString(),
                name,
                date,
                location,
                category,
                capacity
        );
        EventUtils.validateEvent(event);

        return event;
    }

    public Event updateEvent(Event existing, String name, String location, String category, String capacityStr, String dateStr) throws Exception {
        int capacity = Integer.parseInt(capacityStr);
        Date date = DateParser.parse(dateStr);

        Event updated = new Event(
                existing.getEventID(),
                name,
                date,
                location,
                category,
                capacity
        );
        updated.setCancelled(existing.isCancelled());
        updated.setBookedSeats(existing.getBookedSeats());
        EventUtils.validateEvent(updated);

        return updated;
    }

    public void cancel(Event event) {
        EventUtils.cancelEvent(event);
    }
}