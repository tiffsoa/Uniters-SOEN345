package com.example.ticketreservationapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.models.Administrator;
import com.example.ticketreservationapp.models.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AdminDashboardActivity extends AppCompatActivity {

    private EditText etName, etLocation, etCategory, etCapacity, etEventDate;
    private Button btnAdd, btnEdit, btnCancel;

    private RecyclerView rvEvents;
    private AdminEventAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private ListenerRegistration eventsListener;

    private Administrator administrator;
    private Event selectedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        etName = findViewById(R.id.etEventName);
        etLocation = findViewById(R.id.etEventLocation);
        etCategory = findViewById(R.id.etEventCategory);
        etCapacity = findViewById(R.id.etEventCapacity);
        etEventDate = findViewById(R.id.etEventDate);

        btnAdd = findViewById(R.id.btnAddEvent);
        btnEdit = findViewById(R.id.btnEditEvent);
        btnCancel = findViewById(R.id.btnDeleteEvent);

        rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminEventAdapter();
        rvEvents.setAdapter(adapter);

        // Initialize the Administrator object
        administrator = new Administrator("adminId", "admin@example.com", "1234567890");

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");

        // Set up listener for when an event is selected
        adapter.setOnItemClickListener(event -> {
            selectedEvent = administrator.getEvent(event.getEventID());
            populateFields(event);
        });

        // Listen for events in Firestore
        listenToEvents();

        btnAdd.setOnClickListener(v -> addEvent());
        btnEdit.setOnClickListener(v -> editEvent());
        btnCancel.setOnClickListener(v -> cancelEvent());

        etEventDate.setOnClickListener(v -> showDatePicker());
    }

    private void populateFields(Event event) {
        etName.setText(event.getName());
        etLocation.setText(event.getLocation());
        etCategory.setText(event.getCategory());
        etCapacity.setText(String.valueOf(event.getCapacity()));
        etEventDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(event.getDate()));
    }

    private Event buildEventFromInput(String eventId) {
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String eventDateStr = etEventDate.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(location) ||
                TextUtils.isEmpty(category) || TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(eventDateStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return null;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Capacity must be a number", Toast.LENGTH_SHORT).show();
            return null;
        }

        Date eventDate = null;
        try {
            eventDate = new SimpleDateFormat("dd/MM/yyyy").parse(eventDateStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
        }

        return new Event(eventId, name, eventDate, location, category, capacity);
    }

    private void addEvent() {
        Event event = buildEventFromInput(UUID.randomUUID().toString());
        if (event != null) {
            // Add event to Firestore
            eventsRef.document(event.getEventID()).set(event)
                    .addOnSuccessListener(aVoid -> {
                        // Add event to local catalog using Administrator
                        administrator.addEvent(event);
                        Toast.makeText(this, "Event Added", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void editEvent() {
        if (selectedEvent != null) {
            Event updatedEvent = buildEventFromInput(selectedEvent.getEventID());
            if (updatedEvent != null) {

                updatedEvent.setEventID(selectedEvent.getEventID());

                eventsRef.document(selectedEvent.getEventID())
                        .update(
                                "name", updatedEvent.getName(),
                                "date", updatedEvent.getDate(),
                                "location", updatedEvent.getLocation(),
                                "category", updatedEvent.getCategory(),
                                "capacity", updatedEvent.getCapacity()
                        )
                        .addOnSuccessListener(aVoid -> {
                            administrator.editEvent(updatedEvent);
                            Toast.makeText(this, "Event Updated", Toast.LENGTH_SHORT).show();
                            clearFields();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        } else {
            Toast.makeText(this, "Please select an event to edit", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelEvent() {
        if (selectedEvent != null) {
            // Mark the event as canceled
            administrator.cancelEvent(selectedEvent);

            // Update Firestore to reflect the cancellation
            eventsRef.document(selectedEvent.getEventID())
                    .set(selectedEvent)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Event Canceled", Toast.LENGTH_SHORT).show();
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please select an event to cancel", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etName.setText("");
        etLocation.setText("");
        etCategory.setText("");
        etCapacity.setText("");
        etEventDate.setText("");
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String dateString = (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "/"
                            + (monthOfYear + 1 < 10 ? "0" + (monthOfYear + 1) : (monthOfYear + 1)) + "/"
                            + year1;
                    etEventDate.setText(dateString);
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    private void listenToEvents() {
        eventsListener = eventsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error loading events: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            List<Event> eventList = new ArrayList<>();
            if (value != null && !value.isEmpty()) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Event e = doc.toObject(Event.class);
                    if (e != null) eventList.add(e);
                }
            }
            adapter.setEvents(eventList);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventsListener != null) eventsListener.remove();
    }
}