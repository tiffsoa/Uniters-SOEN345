package com.example.ticketreservationapp.data;

import com.example.ticketreservationapp.models.Event;
import com.google.firebase.firestore.*;

//created this separate file to avoid tight coupling, its the data access layer
//in the modifying methods (updating and cancelling, not adding): using transactions to support user concurrency
public class EventRepository {
    private final FirebaseFirestore db;
    private final CollectionReference ref;

    public EventRepository(CollectionReference ref) {
        this.ref = ref;
        this.db = FirebaseFirestore.getInstance();
    }

    public void create(Event event, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        ref.document(event.getEventID())
                .set(event)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void update(Event event, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        db.runTransaction(transaction -> {
                    DocumentReference docRef = ref.document(event.getEventID());
                    Event latest = transaction.get(docRef).toObject(Event.class);

                    if (latest == null) {
                        throw new RuntimeException("Event no longer exists");
                    }
                    event.setCancelled(latest.isCancelled());
                    transaction.set(docRef, event);

                    return null;
                }).addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }

    public void cancel(Event event, Runnable onSuccess, java.util.function.Consumer<String> onError) {
        db.runTransaction(transaction -> {
                    DocumentReference docRef = ref.document(event.getEventID());
                    Event latest = transaction.get(docRef).toObject(Event.class);
                    if (latest == null) {
                        throw new RuntimeException("Event no longer exists");
                    }

                    latest.setCancelled(true);
                    transaction.set(docRef, latest);
                    return null;

                }).addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
    public void getAllEvents(java.util.function.Consumer<java.util.List<Event>> onSuccess,
                         java.util.function.Consumer<String> onError) {

    ref.get()
        .addOnSuccessListener(querySnapshot -> {
            java.util.List<Event> events = new java.util.ArrayList<>();

            for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Event event = doc.toObject(Event.class);

                if (event != null && !event.isCancelled()) {
                    events.add(event);
                }
            }

            onSuccess.accept(events);
        })
        .addOnFailureListener(e -> onError.accept(e.getMessage()));
    }
}
