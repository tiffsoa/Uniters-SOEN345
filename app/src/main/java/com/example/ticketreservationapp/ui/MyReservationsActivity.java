package com.example.ticketreservationapp.ui;

import android.content.Intent;
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
import java.util.List;
import java.util.Map;

// ---
// Displays the customer's reservations with real-time updates
// Uses firestore snapshot listener on Reservations collection filtered by customerID
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
                        reservationAdapter.setReservations(new ArrayList<>());
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
                    reservationAdapter.setReservations(reservations);

                    Log.d(TAG, "Loaded " + reservations.size() + " reservations (real-time)");
                });
    }

    @Override
    public void onCancelClick(Reservation reservation) {
        // Show confirmation dialog before cancellation
        new AlertDialog.Builder(this)
                .setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel your reservation for "
                        + reservation.getEventName() + " (" + reservation.getQuantity() + " ticket(s))?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    performCancellation(reservation);
                })
                .setNegativeButton("No, Keep It", null)
                .show();
    }

    // Calls the cancelReservation cloud function 
    // All cancellation logic (marking reservation/tickets as canceled, decrementing capacity) happens server-side
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
