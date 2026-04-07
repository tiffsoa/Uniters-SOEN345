package com.example.ticketreservationapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

// ---
// Booking screen: Customer selects ticket quantity and confirms the booking
// All booking logic executes server-side in the makeReservation cloud function
// No booking logic runs on the client, only the cloud function call
// ---

public class BookingActivity extends AppCompatActivity {

    private static final String TAG = "BookingActivity";

    private TextView tvEventName, tvEventDate, tvEventLocation, tvAvailableSeats;
    private TextView tvQuantity, tvBookingResult;
    private Button btnDecrease, btnIncrease, btnConfirmBooking;
    private ProgressBar progressBooking;

    private FirebaseFunctions functions;

    private String eventID;
    private String eventName;
    private int availableSeats;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        functions = FirebaseFunctions.getInstance();

        // Get event data
        eventID = getIntent().getStringExtra("eventID");
        eventName = getIntent().getStringExtra("eventName");
        String eventDate = getIntent().getStringExtra("eventDate");
        String eventLocation = getIntent().getStringExtra("eventLocation");
        int capacity = getIntent().getIntExtra("eventCapacity", 0);
        int bookedSeats = getIntent().getIntExtra("eventBookedSeats", 0);
        availableSeats = capacity - bookedSeats;

        // Bind views
        tvEventName = findViewById(R.id.tvBookingEventName);
        tvEventDate = findViewById(R.id.tvBookingEventDate);
        tvEventLocation = findViewById(R.id.tvBookingEventLocation);
        tvAvailableSeats = findViewById(R.id.tvBookingAvailableSeats);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvBookingResult = findViewById(R.id.tvBookingResult);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        progressBooking = findViewById(R.id.progressBooking);

        // Populate event details
        tvEventName.setText(eventName);
        tvEventDate.setText("📅 " + eventDate);
        tvEventLocation.setText("📍 " + eventLocation);
        tvAvailableSeats.setText("Available: " + availableSeats + " seats");
        tvQuantity.setText(String.valueOf(quantity));

        // Quantity controls
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnIncrease.setOnClickListener(v -> {
            int maxQty = Math.min(availableSeats, 10);
            if (quantity < maxQty) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // Confirm booking button calls the makeReservation cloud function
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    // Calls the makeReservation cloud function
    // All capacity checks and firestore transactions happen server-side 
    // Disables UI during request to prevent multiple clicks
    private void confirmBooking() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "You must be signed in.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        btnConfirmBooking.setEnabled(false);
        btnDecrease.setEnabled(false);
        btnIncrease.setEnabled(false);
        progressBooking.setVisibility(View.VISIBLE);
        tvBookingResult.setVisibility(View.GONE);

        Map<String, Object> data = new HashMap<>();
        data.put("eventID", eventID);
        data.put("quantity", quantity);

        functions
                .getHttpsCallable("makeReservation")
                .call(data)
                .addOnSuccessListener(result -> {
                    progressBooking.setVisibility(View.GONE);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) result.getData();
                    String message = (String) response.get("message");
                    String reservationID = (String) response.get("reservationID");

                    tvBookingResult.setText(message + "\nReservation ID: " + reservationID);
                    tvBookingResult.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Booking success: " + reservationID);

                    btnConfirmBooking.setText("Booked");
                })
                .addOnFailureListener(e -> {
                    progressBooking.setVisibility(View.GONE);
                    btnConfirmBooking.setEnabled(true);
                    btnDecrease.setEnabled(true);
                    btnIncrease.setEnabled(true);

                    String errorMsg = e.getMessage();
                    tvBookingResult.setText(errorMsg);
                    tvBookingResult.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Booking failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Booking failed", e);
                });
    }
}
