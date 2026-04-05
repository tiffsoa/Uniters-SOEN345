package com.example.ticketreservationapp.models;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventsCatalog {

    private static EventsCatalog instance;
    private List<Event> events;

    private EventsCatalog() {
        events = new ArrayList<>();
    }

    public static EventsCatalog getInstance() {
        if (instance == null) {
            instance = new EventsCatalog();
        }
        return instance;
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public void removeEvent(Event event) {
        events.removeIf(e -> e.getEventID().equals(event.getEventID()));
    }

    public Event getEvent(String eventID) {
        for (Event e : events) {
            if (e.getEventID().equals(eventID)) {
                return e;
            }
        }
        return null;
    }

    private boolean sameDay(Date d1, Date d2) {
        java.util.Calendar c1 = java.util.Calendar.getInstance();
        java.util.Calendar c2 = java.util.Calendar.getInstance();

        c1.setTime(d1);
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public List<Event> searchEvents(Date date, String location, String category) {
        return events.stream()
                .filter(e ->
                        (date == null|| sameDay(e.getDate(), date)) &&
                                (location == null || e.getLocation().equalsIgnoreCase(location)) &&
                                (category == null || e.getCategory().equalsIgnoreCase(category))
                )
                .collect(Collectors.toList());
    }

    public List<Event> getAllEvents() {
        return events;
    } // unused for now
}