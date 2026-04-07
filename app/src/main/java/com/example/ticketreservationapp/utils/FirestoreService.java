package com.example.ticketreservationapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirestoreService {

    private final FirebaseFirestore db;

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Firestore is working");
        data.put("timestamp", System.currentTimeMillis());

        db.collection("test").add(data);
    }
}
