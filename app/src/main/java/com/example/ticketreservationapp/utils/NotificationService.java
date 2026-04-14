package com.example.ticketreservationapp.utils;

import android.util.Base64;
import android.util.Log;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Locale;

// Sends a booking confirmation via email (Brevo REST API) for email-registered customers
// or via SMS (Twilio REST API) for phone-registered customers
public class NotificationService {

    private static final String TAG = "NotificationService";

    // Main entry point: call after a reservation is made successfully
    // Pass the customer's email and phone as retrieved from FirebaseAuth
    public void sendBookingConfirmation(String email, String phone, Event event, Reservation reservation) {
        if (email != null && !email.isEmpty()) {
            sendEmailAsync(email, event, reservation);
        } else if (phone != null && !phone.isEmpty()) {
            sendSmsAsync(phone, event, reservation);
        } else {
            Log.w(TAG, "No email or phone available for confirmation");
        }
    }

    // Email via Brevo REST API
    void sendEmailAsync(String toEmail, Event event, Reservation reservation) {
        new Thread(() -> {
            try {
                String jsonBody = "{"
                        + "\"sender\":{\"name\":\"" + NotificationConfig.SENDER_NAME + "\","
                        + "\"email\":\"" + NotificationConfig.SENDER_EMAIL + "\"},"
                        + "\"to\":[{\"email\":\"" + toEmail + "\"}],"
                        + "\"subject\":\"Booking Confirmed: " + escapeJson(event.getName()) + "\","
                        + "\"textContent\":\"" + escapeJson(buildMessageBody(event, reservation)) + "\""
                        + "}";

                URL url = new URL("https://api.brevo.com/v3/smtp/email");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("api-key", NotificationConfig.BREVO_API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.flush();

                int code = conn.getResponseCode();
                Log.d(TAG, "Email response code: " + code);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Email failed: " + e.getMessage());
            }
        }).start();
    }

    String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // SMS via Twilio REST API (no SDK needed)
    void sendSmsAsync(String toPhone, Event event, Reservation reservation) {
        new Thread(() -> {
            try {
                String apiUrl = "https://api.twilio.com/2010-04-01/Accounts/"
                        + NotificationConfig.TWILIO_ACCOUNT_SID + "/Messages.json";

                String body = "From=" + URLEncoder.encode(NotificationConfig.TWILIO_FROM_NUMBER, "UTF-8")
                        + "&To=" + URLEncoder.encode(toPhone, "UTF-8")
                        + "&Body=" + URLEncoder.encode(buildMessageBody(event, reservation), "UTF-8");

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String credentials = NotificationConfig.TWILIO_ACCOUNT_SID
                        + ":" + NotificationConfig.TWILIO_AUTH_TOKEN;
                String encoded = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                conn.setRequestProperty("Authorization", "Basic " + encoded);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("UTF-8"));
                os.flush();

                int code = conn.getResponseCode();
                Log.d(TAG, "SMS response code: " + code);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "SMS failed: " + e.getMessage());
            }
        }).start();
    }

    String buildMessageBody(Event event, Reservation reservation) {
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(event.getDate());
        return "Hello,\n\n"
                + "Your booking has been confirmed!\n\n"
                + "Event Details:\n"
                + "  Name:     " + event.getName() + "\n"
                + "  Date:     " + date + "\n"
                + "  Location: " + event.getLocation() + "\n"
                + "  Category: " + event.getCategory() + "\n\n"
                + "Reservation Details:\n"
                + "  Reservation ID: " + reservation.getReservationID() + "\n"
                + "  Tickets:        " + reservation.getTicketCount() + "\n\n"
                + "Thank you for booking with Uniters!";
    }
}
