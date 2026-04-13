package com.example.ticketreservationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.ui.LoginActivity;
import com.example.ticketreservationapp.ui.EventsCatalogActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.ticketreservationapp.utils.FirestoreService;
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

             Intent intent = new Intent(MainActivity.this, EventsCatalogActivity.class);
             startActivity(intent);
             finish();

        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
