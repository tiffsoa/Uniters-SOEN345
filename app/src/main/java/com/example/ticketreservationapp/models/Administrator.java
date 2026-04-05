package com.example.ticketreservationapp.models;

import java.util.Date;
import java.util.List;

public class Administrator extends User {

    private EventsCatalog eventsCatalog;

    public Administrator(String userID, String email, String phoneNumber) {
        super(userID, email, phoneNumber);
        this.eventsCatalog = EventsCatalog.getInstance(); // Singleton pattern
    }

    // Adds an event to the local catalog
    public void addEvent(Event event) {
        eventsCatalog.addEvent(event);
    }

    // Edits an event in the local catalog
    public void editEvent(Event updatedEvent) {
        Event existing = eventsCatalog.getEvent(updatedEvent.getEventID());
        if (existing != null) {
            existing.updateDetails(
                    updatedEvent.getName(),
                    updatedEvent.getDate(),
                    updatedEvent.getLocation()
            );
        }
    }

    // Cancel event in the local catalog
    public void cancelEvent(Event event) {
        event.cancel(); // Cancel the event (mark it as canceled)
    }

    // Retrieves an event by ID from the local catalog
    public Event getEvent(String eventID) {
        return eventsCatalog.getEvent(eventID);
    }

    // Searches for events locally (could be enhanced to search Firestore too if needed)
    public List<Event> searchEvents(Date date, String location, String category) {
        return eventsCatalog.searchEvents(date, location, category);
    }
}