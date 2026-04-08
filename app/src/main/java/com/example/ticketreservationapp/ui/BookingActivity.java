package com.example.ticketreservationapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ---
// Booking screen: customer selects ticket count from a dropdown and confirms the booking
// All booking logic executes server-side in the makeReservation cloud function
// No booking logic runs on the client, only the cloud function call
// ---

public class BookingActivity extends AppCompatActivity {

    private static final String TAG = "BookingActivity";

    private TextView tvEventName, tvEventDate, tvEventLocation, tvAvailableSeats;
    private TextView tvBookingResult;
    private Spinner spinnerTicketCount;
    private Button btnConfirmBooking;
    private ProgressBar progressBooking;

    private FirebaseFunctions functions;

    private String eventID;
    private String eventName;
    private int remainingCapacity;
    private int ticketCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        functions = FirebaseFunctions.getInstance();

        // Get event data from intent extras
        eventID = getIntent().getStringExtra("eventID");
        eventName = getIntent().getStringExtra("eventName");
        String eventDate = getIntent().getStringExtra("eventDate");
        String eventLocation = getIntent().getStringExtra("eventLocation");
        remainingCapacity = getIntent().getIntExtra("remainingCapacity", 0);
        int maxCapacity = getIntent().getIntExtra("maxCapacity", 0);

        // Bind views
        tvEventName = findViewById(R.id.tvBookingEventName);
        tvEventDate = findViewById(R.id.tvBookingEventDate);
        tvEventLocation = findViewById(R.id.tvBookingEventLocation);
        tvAvailableSeats = findViewById(R.id.tvBookingAvailableSeats);
        spinnerTicketCount = findViewById(R.id.spinnerTicketCount);
        tvBookingResult = findViewById(R.id.tvBookingResult);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        progressBooking = findViewById(R.id.progressBooking);

        // Populate event details
        tvEventName.setText(eventName);
        tvEventDate.setText(eventDate);
        tvEventLocation.setText(eventLocation);
        tvAvailableSeats.setText("Available: " + remainingCapacity + " / " + maxCapacity + " seats");

        // Set up ticket count dropdown (1 to min(8, remainingCapacity))
        setupTicketSpinner();

        // Confirm booking button calls the makeReservation cloud function
        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    // Populates the spinner with ticket count options from 1 to min(8, remainingCapacity)
    private void setupTicketSpinner() {
        int maxTickets = Math.min(8, remainingCapacity);
        List<String> options = new ArrayList<>();
        for (int i = 1; i <= maxTickets; i++) {
            options.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTicketCount.setAdapter(adapter);

        spinnerTicketCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ticketCount = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ticketCount = 1;
            }
        });
    }

    // Calls the makeReservation cloud function
    // All capacity checks and firestore transactions happen server-side
    // Disables UI during the request to prevent multiple clicks
    private void confirmBooking() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "You must be signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmBooking.setEnabled(false);
        spinnerTicketCount.setEnabled(false);
        progressBooking.setVisibility(View.VISIBLE);
        tvBookingResult.setVisibility(View.GONE);

        Map<String, Object> data = new HashMap<>();
        data.put("eventID", eventID);
        data.put("ticketCount", ticketCount);

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
                    spinnerTicketCount.setEnabled(true);

                    String errorMsg = e.getMessage();
                    tvBookingResult.setText(errorMsg);
                    tvBookingResult.setVisibility(View.VISIBLE);

                    Toast.makeText(this, "Booking failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Booking failed", e);
                });
    }
}
