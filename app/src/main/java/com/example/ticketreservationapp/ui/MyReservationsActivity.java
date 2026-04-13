package com.example.ticketreservationapp.ui;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.data.ReservationRepository;
import com.example.ticketreservationapp.domain.ReservationService;
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

// Shows all reservations for the logged-in customer, with cancel option
public class MyReservationsActivity extends AppCompatActivity {

    private RecyclerView rvReservations;
    private Button btnBack;

    private ReservationAdapter adapter;
    private ReservationRepository repository;
    private ReservationService service;

    private FirebaseFirestore db;
    private CollectionReference reservationsRef, eventsRef;

    private String customerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        db = FirebaseFirestore.getInstance();
        reservationsRef = db.collection("Reservations");
        eventsRef = db.collection("Events");
        repository = new ReservationRepository(reservationsRef, eventsRef);
        service = new ReservationService();

        customerID = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        bindViews();
        setupRecycler();
        setupListeners();

        if (customerID != null) {
            listenToReservations();
        } else {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindViews() {
        rvReservations = findViewById(R.id.rvReservations);
        btnBack = findViewById(R.id.btnBackToEvents);
    }

    private void setupRecycler() {
        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter();

        adapter.setOnCancelClickListener(reservation -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Reservation")
                    .setMessage("Are you sure you want to cancel this reservation?")
                    .setPositiveButton("Yes", (dialog, which) -> cancelReservation(reservation))
                    .setNegativeButton("No", null)
                    .show();
        });

        rvReservations.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void listenToReservations() {
        // Real-time listener filtered by customerID
        reservationsRef.whereEqualTo("customerID", customerID)
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;

                    List<Reservation> list = new ArrayList<>();
                    Set<String> eventIDs = new HashSet<>();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Reservation r = doc.toObject(Reservation.class);
                        if (r != null) {
                            list.add(r);
                            eventIDs.add(r.getEventID());
                        }
                    }

                    // Fetch event names for display
                    if (eventIDs.isEmpty()) {
                        adapter.setReservations(list);
                        return;
                    }

                    fetchEventNames(eventIDs, list);
                });
    }

    private void fetchEventNames(Set<String> eventIDs, List<Reservation> reservations) {
        Map<String, String> nameMap = new HashMap<>();
        // Firestore whereIn supports max 30 items per query
        List<String> ids = new ArrayList<>(eventIDs);

        eventsRef.whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            nameMap.put(event.getEventID(), event.getName());
                        }
                    }
                    adapter.setEventNameMap(nameMap);
                    adapter.setReservations(reservations);
                })
                .addOnFailureListener(e -> {
                    // Fallback: show without event names
                    adapter.setReservations(reservations);
                });
    }

    private void cancelReservation(Reservation reservation) {
        try {
            service.cancel(reservation);

            repository.cancel(reservation,
                    () -> Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show(),
                    msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            );
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
