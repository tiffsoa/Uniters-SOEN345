package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.models.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EventUtils {

    public static List<Event> filter(List<Event> events, Date date, String location, String category) {
        return events.stream()
                .filter(e ->
                        (date == null || sameDay(e.getDate(), date)) &&
                                (location == null || e.getLocation().equalsIgnoreCase(location)) &&
                                (category == null || e.getCategory().equalsIgnoreCase(category))
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