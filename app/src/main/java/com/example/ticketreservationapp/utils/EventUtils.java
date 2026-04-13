package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.models.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EventUtils {
    //moved domain validation in the event utils
    public static void validateEvent(Event event) {

        if (event == null)
            throw new IllegalArgumentException("Event cannot be null");

        if (event.getName() == null || event.getName().trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty");

        if (event.getLocation() == null || event.getLocation().trim().isEmpty())
            throw new IllegalArgumentException("Location cannot be empty");

        if (event.getCategory() == null || event.getCategory().trim().isEmpty())
            throw new IllegalArgumentException("Category cannot be empty");

        if (event.getCapacity() <= 0)
            throw new IllegalArgumentException("Capacity must be > 0");

        if (event.getDate() == null || event.getDate().before(new Date()))
            throw new IllegalArgumentException("Event date cannot be in past");
    }

    public static void cancelEvent(Event event) {
        event.setCancelled(true);
    }

    public static List<Event> filter(List<Event> events, Date date, String location, String category) {
        return events.stream()
                .filter(e ->
                        (date == null || sameDay(e.getDate(), date)) &&
                                (location == null || e.getLocation().toLowerCase().contains(location.toLowerCase())) &&
                                (category == null || e.getCategory().toLowerCase().contains(category.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    private static boolean sameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(d1);
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}