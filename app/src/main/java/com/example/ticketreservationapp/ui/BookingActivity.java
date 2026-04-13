package com.example.ticketreservationapp.ui;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.data.ReservationRepository;
import com.example.ticketreservationapp.domain.ReservationService;
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.Locale;

// Booking screen: shows event details, lets customer pick ticket count and confirm
public class BookingActivity extends AppCompatActivity {

    private TextView tvEventName, tvEventDetails, tvEventDate, tvAvailableSeats;
    private EditText etTicketCount;
    private Button btnConfirm, btnBack;

    private FirebaseFirestore db;
    private CollectionReference eventsRef, reservationsRef;
    private ReservationRepository repository;
    private ReservationService service;

    private String eventID;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        eventID = getIntent().getStringExtra("eventID");
        if (eventID == null) {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");
        reservationsRef = db.collection("Reservations");
        repository = new ReservationRepository(reservationsRef, eventsRef);
        service = new ReservationService();

        bindViews();
        setupListeners();
        listenToEvent();
    }

    private void bindViews() {
        tvEventName = findViewById(R.id.tvBookingEventName);
        tvEventDetails = findViewById(R.id.tvBookingEventDetails);
        tvEventDate = findViewById(R.id.tvBookingEventDate);
        tvAvailableSeats = findViewById(R.id.tvBookingAvailableSeats);
        etTicketCount = findViewById(R.id.etTicketCount);
        btnConfirm = findViewById(R.id.btnConfirmBooking);
        btnBack = findViewById(R.id.btnBackToCatalog);
    }

    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> confirmBooking());
        btnBack.setOnClickListener(v -> finish());
    }

    private void listenToEvent() {
        //real-time listener so available seats update live
        eventsRef.document(eventID).addSnapshotListener((snapshot, error) -> {
            if (snapshot != null && snapshot.exists()) {
                currentEvent = snapshot.toObject(Event.class);
                if (currentEvent != null) {
                    populateEventDetails();
                }
            }
        });
    }

    private void populateEventDetails() {
        tvEventName.setText(currentEvent.getName());
        tvEventDetails.setText(currentEvent.getLocation() + " - " + currentEvent.getCategory());
        tvEventDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentEvent.getDate()));

        int remaining = currentEvent.getCapacity() - currentEvent.getBookedSeats();
        tvAvailableSeats.setText("Available seats: " + remaining);

        if (currentEvent.isCancelled()) {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Event Cancelled");
        } else if (remaining <= 0) {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Sold Out");
        } else {
            btnConfirm.setEnabled(true);
            btnConfirm.setText("Confirm Booking");
        }
    }

    private void confirmBooking() {
        String ticketStr = etTicketCount.getText().toString().trim();
        if (ticketStr.isEmpty()) {
            Toast.makeText(this, "Enter number of tickets", Toast.LENGTH_SHORT).show();
            return;
        }

        int ticketCount;
        try {
            ticketCount = Integer.parseInt(ticketStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        String customerID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (customerID == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Reservation reservation = service.createReservation(eventID, customerID, ticketCount, currentEvent);

            btnConfirm.setEnabled(false);
            repository.book(reservation,
                    () -> {
                        Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    msg -> {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true);
                    }
            );
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
