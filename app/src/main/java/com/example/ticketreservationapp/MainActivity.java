package com.example.ticketreservationapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.ui.AdminDashboardActivity;
import com.example.ticketreservationapp.ui.CustomerDashboardActivity;
import com.example.ticketreservationapp.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is already signed in: look up their role and route accordingly
            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String role = doc.getString("role");
                        Intent intent;
                        if ("administrator".equalsIgnoreCase(role)) {
                            intent = new Intent(this, AdminDashboardActivity.class);
                        } else {
                            intent = new Intent(this, CustomerDashboardActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Role fetch failed: fall back to login
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
