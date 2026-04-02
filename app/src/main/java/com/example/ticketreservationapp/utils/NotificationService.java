package com.example.ticketreservationapp.utils;

import android.util.Log;

public class NotificationService {

    public void sendConfirmation(String userId) {
        Log.d("NotificationService", "Confirmation sent to user: " + userId);
    }
}
