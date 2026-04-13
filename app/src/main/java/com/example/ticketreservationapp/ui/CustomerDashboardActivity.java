package com.example.ticketreservationapp.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.utils.EventUtils;
import com.google.firebase.firestore.*;

import java.util.*;

// Customer landing page: browse events, filter, navigate to booking or reservations
public class CustomerDashboardActivity extends AppCompatActivity {

    private EditText etFilterLocation, etFilterCategory, etFilterDate;
    private Button btnApplyFilter, btnMyReservations, btnLogout;
    private RecyclerView rvCustomerEvents;

    private CustomerEventAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    private List<Event> allEvents = new ArrayList<>();

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused instanceof EditText) {
                android.graphics.Rect rect = new android.graphics.Rect();
                focused.getGlobalVisibleRect(rect);
                if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    focused.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

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
        etFilterDate = findViewById(R.id.etFilterDate);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnMyReservations = findViewById(R.id.btnMyReservations);
        btnLogout = findViewById(R.id.btnCustomerLogout);
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
        setupClearButton(etFilterLocation);
        setupClearButton(etFilterCategory);
        setupClearButton(etFilterDate);

        btnApplyFilter.setOnClickListener(v -> applyFilter());

        etFilterDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this,
                    (view, y, m, d) -> etFilterDate.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(java.util.Calendar.YEAR),
                    c.get(java.util.Calendar.MONTH),
                    c.get(java.util.Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnMyReservations.setOnClickListener(v -> {
            startActivity(new Intent(this, MyReservationsActivity.class));
        });

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
        String dateStr = etFilterDate.getText().toString().trim();

        java.util.Date filterDate = null;
        if (!dateStr.isEmpty()) {
            try {
                filterDate = com.example.ticketreservationapp.utils.DateParser.parse(dateStr);
            } catch (Exception ignored) { }
        }

        List<Event> filtered = EventUtils.filter(
                allEvents,
                filterDate,
                location.isEmpty() ? null : location,
                category.isEmpty() ? null : category
        );

        adapter.setEvents(filtered);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClearButton(EditText editText) {
        Drawable clearIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_close_clear_cancel);
        if (clearIcon != null) {
            clearIcon = clearIcon.mutate();
            clearIcon.setTint(Color.GRAY);
            int size = (int) (18 * getResources().getDisplayMetrics().density);
            clearIcon.setBounds(0, 0, size, size);
        }
        final Drawable icon = clearIcon;

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    editText.setCompoundDrawablesRelative(null, null, icon, null);
                } else {
                    editText.setCompoundDrawablesRelative(null, null, null, null);
                }
            }
        });

        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable end = editText.getCompoundDrawablesRelative()[2];
                if (end != null) {
                    int iconWidth = end.getBounds().width();
                    if (event.getX() >= editText.getWidth() - editText.getPaddingEnd() - iconWidth - 16) {
                        editText.setText("");
                        applyFilter();
                        return true;
                    }
                }
            }
            return false;
        });
    }
}
