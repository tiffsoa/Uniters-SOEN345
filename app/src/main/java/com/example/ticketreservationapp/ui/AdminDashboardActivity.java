package com.example.ticketreservationapp.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.data.EventRepository;
import com.example.ticketreservationapp.domain.EventService;
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.utils.InputValidator;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

//removed anything other than interaction, it is onlky a UI layer
public class AdminDashboardActivity extends AppCompatActivity {

    private EditText etName, etLocation, etCategory, etCapacity, etEventDate;
    private LinearLayout formContainer;
    private Button btnOpenAddForm, btnSaveEvent, btnCloseForm, btnCancel, btnLogout;
    private RecyclerView rvEvents;

    private AdminEventAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private EventRepository repository;
    private EventService service;

    private Event selectedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        service = new EventService();
        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("Events");
        repository = new EventRepository(eventsRef);

        bindViews();
        setupRecycler();
        setupListeners();
        listenToEvents();
    }

    private void setupListeners() {

        btnOpenAddForm.setOnClickListener(v -> {
            selectedEvent = null;
            clearFields();
            showForm();
        });

        btnSaveEvent.setOnClickListener(v -> {
            try {
                InputValidator.validate(
                        etName.getText().toString(),
                        etLocation.getText().toString(),
                        etCategory.getText().toString(),
                        etCapacity.getText().toString(),
                        etEventDate.getText().toString()
                );
                Event event;
                if (selectedEvent == null) {
                    event = service.createEvent(
                            etName.getText().toString(),
                            etLocation.getText().toString(),
                            etCategory.getText().toString(),
                            etCapacity.getText().toString(),
                            etEventDate.getText().toString()
                    );
                } else {
                    event = service.updateEvent(
                            selectedEvent,
                            etName.getText().toString(),
                            etLocation.getText().toString(),
                            etCategory.getText().toString(),
                            etCapacity.getText().toString(),
                            etEventDate.getText().toString()
                    );
                }

                if (selectedEvent == null) {
                    repository.create(event,
                            () -> Toast.makeText(this, "Event Created", Toast.LENGTH_SHORT).show(),
                            msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    );
                } else {
                    repository.update(event,
                            () -> Toast.makeText(this, "Event Updated", Toast.LENGTH_SHORT).show(),
                            msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    );
                }

                hideForm();

            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (selectedEvent == null) return;

            service.cancel(selectedEvent);

            repository.cancel(selectedEvent,
                    () -> Toast.makeText(this, "Event Canceled", Toast.LENGTH_SHORT).show(),
                    msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            );

            hideForm();
        });

        btnCloseForm.setOnClickListener(v -> hideForm());

        etEventDate.setOnClickListener(v -> showDatePicker());

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void listenToEvents() {
        eventsRef.addSnapshotListener((value, error) -> {

            List<Event> list = new ArrayList<>();

            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Event e = doc.toObject(Event.class);
                    if (e != null) list.add(e);
                }
            }

            adapter.setEvents(list);
        });
    }

    private void setupRecycler() {
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminEventAdapter();

        adapter.setOnItemClickListener(event -> {
            selectedEvent = event;
            populateFields(event);
            showForm();
        });

        rvEvents.setAdapter(adapter);
    }

    private void populateFields(Event e) {
        etName.setText(e.getName());
        etLocation.setText(e.getLocation());
        etCategory.setText(e.getCategory());
        etCapacity.setText(String.valueOf(e.getCapacity()));
        etEventDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(e.getDate()));
    }

    private void clearFields() {
        etName.setText("");
        etLocation.setText("");
        etCategory.setText("");
        etCapacity.setText("");
        etEventDate.setText("");
    }

    private void showForm() {
        formContainer.setVisibility(View.VISIBLE);
        btnOpenAddForm.setVisibility(View.GONE);
    }

    private void hideForm() {
        formContainer.setVisibility(View.GONE);
        btnOpenAddForm.setVisibility(View.VISIBLE);
        clearFields();
        selectedEvent = null;
    }

    private void bindViews() {
        etName = findViewById(R.id.etEventName);
        etLocation = findViewById(R.id.etEventLocation);
        etCategory = findViewById(R.id.etEventCategory);
        etCapacity = findViewById(R.id.etEventCapacity);
        etEventDate = findViewById(R.id.etEventDate);

        formContainer = findViewById(R.id.formContainer);
        btnOpenAddForm = findViewById(R.id.btnOpenAddForm);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnCloseForm = findViewById(R.id.btnCloseForm);
        btnCancel = findViewById(R.id.btnCancelEvent);
        btnLogout = findViewById(R.id.btnAdminLogout);
        rvEvents = findViewById(R.id.rvEvents);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this,
                (v, y, m, d) -> etEventDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }
}