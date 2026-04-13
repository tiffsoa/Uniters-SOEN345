package com.example.ticketreservationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.ticketreservationapp.utils.FirestoreService;

import android.widget.TextView;
import android.util.Log;

import com.example.ticketreservationapp.data.EventRepository;
import com.example.ticketreservationapp.models.Event;

import com.google.firebase.firestore.FirebaseFirestore;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        new FirestoreService().saveTestData();
        //check if user is signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            //show main dashboard that lists events
            Toast.makeText(this, "Welcome back! Routing to Dashboard...", Toast.LENGTH_SHORT).show();
            TextView textView = findViewById(R.id.textView);

EventRepository repo = new EventRepository(
        FirebaseFirestore.getInstance().collection("events")
);

repo.getAllEvents(events -> {

    StringBuilder display = new StringBuilder();

    for (Event e : events) {
        if (e.getCapacity() > e.getBookedSeats()) {
            display.append(e.getName()).append("\n");
        }
    }

    runOnUiThread(() -> textView.setText(display.toString()));

}, error -> {
    Log.e("ERROR", error);
});

            // Intent intent = new Intent(MainActivity.this, EventsCatalogActivity.class);
            // startActivity(intent);
            // finish();

        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
