package com.example.ticketreservationapp.data;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import com.google.firebase.firestore.*;

// Data access layer for reservations, mirrors EventRepository's transaction pattern
// book() and cancel() both atomically update the reservation AND the event's bookedSeats
public class ReservationRepository {
    private final FirebaseFirestore db;
    private final CollectionReference reservationsRef;
    private final CollectionReference eventsRef;

    public ReservationRepository(CollectionReference reservationsRef, CollectionReference eventsRef) {
        this.reservationsRef = reservationsRef;
        this.eventsRef = eventsRef;
        this.db = FirebaseFirestore.getInstance();
    }

    public void book(Reservation reservation, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        db.runTransaction(transaction -> {
            // Read the latest event state inside the transaction
            DocumentReference eventDocRef = eventsRef.document(reservation.getEventID());
            Event latestEvent = transaction.get(eventDocRef).toObject(Event.class);

            if (latestEvent == null) {
                throw new RuntimeException("Event no longer exists");
            }
            if (latestEvent.isCancelled()) {
                throw new RuntimeException("Event has been cancelled");
            }

            int remaining = latestEvent.getCapacity() - latestEvent.getBookedSeats();
            if (reservation.getTicketCount() > remaining) {
                throw new RuntimeException("Not enough seats available (remaining: " + remaining + ")");
            }

            // Write the reservation document
            DocumentReference resDocRef = reservationsRef.document(reservation.getReservationID());
            transaction.set(resDocRef, reservation);

            // Increment bookedSeats on the event
            latestEvent.setBookedSeats(latestEvent.getBookedSeats() + reservation.getTicketCount());
            transaction.set(eventDocRef, latestEvent);

            return null;
        }).addOnSuccessListener(aVoid -> onSuccess.run())
          .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void cancel(Reservation reservation, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        db.runTransaction(transaction -> {
            // Read latest reservation state
            DocumentReference resDocRef = reservationsRef.document(reservation.getReservationID());
            Reservation latestRes = transaction.get(resDocRef).toObject(Reservation.class);

            if (latestRes == null) {
                throw new RuntimeException("Reservation no longer exists");
            }
            if (latestRes.isCancelled()) {
                throw new RuntimeException("Reservation is already cancelled");
            }

            // Read latest event state
            DocumentReference eventDocRef = eventsRef.document(reservation.getEventID());
            Event latestEvent = transaction.get(eventDocRef).toObject(Event.class);

            if (latestEvent == null) {
                throw new RuntimeException("Event no longer exists");
            }

            // Mark reservation as cancelled
            latestRes.setCancelled(true);
            transaction.set(resDocRef, latestRes);

            // Decrement bookedSeats on the event
            int updatedSeats = Math.max(0, latestEvent.getBookedSeats() - reservation.getTicketCount());
            latestEvent.setBookedSeats(updatedSeats);
            transaction.set(eventDocRef, latestEvent);

            return null;
        }).addOnSuccessListener(aVoid -> onSuccess.run())
          .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}
