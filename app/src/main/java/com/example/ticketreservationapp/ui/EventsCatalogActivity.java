package com.example.ticketreservationapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.adapters.EventAdapter;
import com.example.ticketreservationapp.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

// ---
// Displays the list of available events
// Uses a real-time firestore snapshot listener on the "Events" collection
// So the UI updates automatically when events are added, modified, or removed
// ---

public class EventsCatalogActivity extends AppCompatActivity implements EventAdapter.OnBookClickListener {

    private static final String TAG = "EventsCatalog";

    private RecyclerView rvEvents;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private EventAdapter eventAdapter;

    private FirebaseFirestore db;
    private ListenerRegistration eventsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_catalog);

        db = FirebaseFirestore.getInstance();

        // Bind views
        rvEvents = findViewById(R.id.rvEvents);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        Button btnMyReservations = findViewById(R.id.btnMyReservations);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Set up RecyclerView 
        eventAdapter = new EventAdapter(this);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(eventAdapter);

        // Navigation
        btnMyReservations.setOnClickListener(v -> {
            startActivity(new Intent(this, MyReservationsActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Start real-time listener
        startEventsListener();
    }

    // Attaches a real-time snapshot listener to the Events collection
    // Shows all events including cancelled ones
    private void startEventsListener() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        eventsListener = db.collection("Events")
                .addSnapshotListener((QuerySnapshot snapshots, com.google.firebase.firestore.FirebaseFirestoreException e) -> {
                    progressBar.setVisibility(View.GONE);

                    if (e != null) {
                        Log.e(TAG, "Error listening to events", e);
                        Toast.makeText(this, "Error loading events.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvEvents.setVisibility(View.GONE);
                        eventAdapter.setEvents(new ArrayList<>());
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventID(doc.getId());
                            events.add(event);
                        }
                    }

                    tvEmptyState.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
                    rvEvents.setVisibility(events.isEmpty() ? View.GONE : View.VISIBLE);
                    eventAdapter.setEvents(events);

                    Log.d(TAG, "Loaded " + events.size() + " events (real-time)");
                });
    }

    @Override
    public void onBookClick(Event event) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("eventID", event.getEventID());
        intent.putExtra("eventName", event.getName());
        intent.putExtra("eventDate", event.getFormattedDate());
        intent.putExtra("eventLocation", event.getLocation());
        intent.putExtra("remainingCapacity", event.getRemainingCapacity());
        intent.putExtra("maxCapacity", event.getMaxCapacity());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsListener != null) {
            eventsListener.remove();
        }
    }
}
