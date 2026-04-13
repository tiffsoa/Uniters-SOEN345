package com.example.ticketreservationapp.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.data.EventRepository;
import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.utils.EventUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.stream.Collectors;

public class EventsCatalogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventRepository repo = new EventRepository(
                FirebaseFirestore.getInstance().collection("events")
        );

        repo.getAllEvents(events -> {

            List<Event> filtered = EventUtils.filter(
                    events,
                    null,
                    null,
                    null
            );

            filtered = filtered.stream()
                    .filter(e -> e.getCapacity() > e.getBookedSeats())
                    .collect(Collectors.toList());

            for (Event e : filtered) {
                Log.d("EVENT", e.getName());
            }

        }, error -> {
            Log.e("ERROR", error);
        });
    }
}
