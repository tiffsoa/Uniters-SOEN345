package com.example.ticketreservationapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.utils.EventUtils;
import com.google.firebase.firestore.*;

import java.util.*;

// Customer landing page: browse events, filter, navigate to booking or reservations
public class CustomerDashboardActivity extends AppCompatActivity {

    private EditText etFilterLocation, etFilterCategory;
    private Button btnApplyFilter, btnMyReservations;
    private RecyclerView rvCustomerEvents;

    private CustomerEventAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        bindViews();
        setupRecycler();
        setupListeners();
        listenToEvents();
    }

    private void bindViews() {
        etFilterLocation = findViewById(R.id.etFilterLocation);
        etFilterCategory = findViewById(R.id.etFilterCategory);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnMyReservations = findViewById(R.id.btnMyReservations);
        rvCustomerEvents = findViewById(R.id.rvCustomerEvents);
    }

    private void setupRecycler() {
        rvCustomerEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerEventAdapter();

        adapter.setOnBookClickListener(event -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("eventID", event.getEventID());
            startActivity(intent);
        });

        rvCustomerEvents.setAdapter(adapter);
    }

    private void setupListeners() {
        btnApplyFilter.setOnClickListener(v -> applyFilter());

        btnMyReservations.setOnClickListener(v -> {
            startActivity(new Intent(this, MyReservationsActivity.class));
        });
    }

    private void listenToEvents() {
        eventsRef.addSnapshotListener((value, error) -> {
            allEvents.clear();

            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Event e = doc.toObject(Event.class);
                    if (e != null) allEvents.add(e);
                }
            }

            applyFilter();
        });
    }

    private void applyFilter() {
        String location = etFilterLocation.getText().toString().trim();
        String category = etFilterCategory.getText().toString().trim();

        List<Event> filtered = EventUtils.filter(
                allEvents,
                null, // no date filter in the UI for now
                location.isEmpty() ? null : location,
                category.isEmpty() ? null : category
        );

        adapter.setEvents(filtered);
    }
}
