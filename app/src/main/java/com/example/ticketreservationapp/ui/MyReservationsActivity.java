package com.example.ticketreservationapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.adapters.ReservationAdapter;
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// ---
// Displays the customer's reservations with real-time updates
// Uses firestore snapshot listener on Reservations collection filtered by customerID
// Event details are fetched separately using eventIDs from the reservations
// Cancellation is handled by calling the cancelReservation cloud function
// ---

public class MyReservationsActivity extends AppCompatActivity implements ReservationAdapter.OnCancelClickListener {

    private static final String TAG = "MyReservations";

    private RecyclerView rvReservations;
    private ProgressBar progressReservations;
    private TextView tvNoReservations;
    private ReservationAdapter reservationAdapter;

    private FirebaseFirestore db;
    private FirebaseFunctions functions;
    private ListenerRegistration reservationsListener;

    // Cached event details keyed by eventID for displaying in reservation cards
    private Map<String, Event> eventMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        db = FirebaseFirestore.getInstance();
        functions = FirebaseFunctions.getInstance();

        // Bind views
        rvReservations = findViewById(R.id.rvReservations);
        progressReservations = findViewById(R.id.progressReservations);
        tvNoReservations = findViewById(R.id.tvNoReservations);
        Button btnBackToEvents = findViewById(R.id.btnBackToEvents);

        // Set up RecyclerView
        reservationAdapter = new ReservationAdapter(this);
        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        rvReservations.setAdapter(reservationAdapter);

        // Navigation
        btnBackToEvents.setOnClickListener(v -> finish());

        // Start real-time listener
        startReservationsListener();
    }

    // Attaches a real-time snapshot listener to Reservations collection
    // Filtered by the current customer's UID and ordered by creation time
    // After loading reservations, fetches corresponding event details
    private void startReservationsListener() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String customerID = currentUser.getUid();
        progressReservations.setVisibility(View.VISIBLE);
        tvNoReservations.setVisibility(View.GONE);

        reservationsListener = db.collection("Reservations")
                .whereEqualTo("customerID", customerID)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((QuerySnapshot snapshots, com.google.firebase.firestore.FirebaseFirestoreException e) -> {
                    progressReservations.setVisibility(View.GONE);

                    if (e != null) {
                        Log.e(TAG, "Error listening to reservations", e);
                        Toast.makeText(this, "Error loading reservations.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        tvNoReservations.setVisibility(View.VISIBLE);
                        rvReservations.setVisibility(View.GONE);
                        reservationAdapter.setData(new ArrayList<>(), new HashMap<>());
                        return;
                    }

                    List<Reservation> reservations = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Reservation reservation = doc.toObject(Reservation.class);
                        if (reservation != null) {
                            reservation.setReservationID(doc.getId());
                            reservations.add(reservation);
                        }
                    }

                    tvNoReservations.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
                    rvReservations.setVisibility(reservations.isEmpty() ? View.GONE : View.VISIBLE);

                    // Fetch event details for all reservations then update adapter
                    fetchEventsForReservations(reservations);

                    Log.d(TAG, "Loaded " + reservations.size() + " reservations (real-time)");
                });
    }

    // Fetches event documents for all unique eventIDs in the reservations list
    // Uses individual document gets and aggregates results into an event map
    private void fetchEventsForReservations(List<Reservation> reservations) {
        Set<String> eventIDs = new HashSet<>();
        for (Reservation r : reservations) {
            if (r.getEventID() != null) {
                eventIDs.add(r.getEventID());
            }
        }

        if (eventIDs.isEmpty()) {
            reservationAdapter.setData(reservations, new HashMap<>());
            return;
        }

        Map<String, Event> newEventMap = new HashMap<>();
        final int[] remaining = {eventIDs.size()};

        for (String eventID : eventIDs) {
            db.collection("Events").document(eventID).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setEventID(doc.getId());
                                newEventMap.put(doc.getId(), event);
                            }
                        }
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            eventMap = newEventMap;
                            reservationAdapter.setData(reservations, eventMap);
                        }
                    })
                    .addOnFailureListener(err -> {
                        Log.e(TAG, "Error fetching event " + eventID, err);
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            eventMap = newEventMap;
                            reservationAdapter.setData(reservations, eventMap);
                        }
                    });
        }
    }

    @Override
    public void onCancelClick(Reservation reservation) {
        // Get event name from cached event map for the confirmation dialog
        Event event = eventMap.get(reservation.getEventID());
        String eventName = event != null ? event.getName() : "this event";

        new AlertDialog.Builder(this)
                .setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel your reservation for "
                        + eventName + " (" + reservation.getTicketCount() + " ticket(s))?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    performCancellation(reservation);
                })
                .setNegativeButton("No, Keep It", null)
                .show();
    }

    // Calls the cancelReservation cloud function
    // All cancellation logic (marking reservation as cancelled, returning capacity) happens server-side
    private void performCancellation(Reservation reservation) {
        progressReservations.setVisibility(View.VISIBLE);

        Map<String, Object> data = new HashMap<>();
        data.put("reservationID", reservation.getReservationID());

        functions
                .getHttpsCallable("cancelReservation")
                .call(data)
                .addOnSuccessListener(result -> {
                    progressReservations.setVisibility(View.GONE);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) result.getData();
                    String message = (String) response.get("message");

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Cancellation success: " + message);
                    // UI updates automatically via firestore listener
                })
                .addOnFailureListener(e -> {
                    progressReservations.setVisibility(View.GONE);

                    String errorMsg = e.getMessage();
                    Toast.makeText(this, "Cancellation failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cancellation failed", e);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reservationsListener != null) {
            reservationsListener.remove();
        }
    }
}
