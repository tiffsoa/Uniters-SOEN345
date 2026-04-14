package com.example.ticketreservationapp.utils;

// Credentials for email and SMS notifications
public class NotificationConfig {

    // Brevo REST API (for email-registered customers)
    public static final String BREVO_API_KEY = "YOUR_BREVO_API_KEY";
    public static final String SENDER_EMAIL = "YOUR_SENDER_EMAIL";
    public static final String SENDER_NAME = "Uniters Tickets";

    // Twilio REST API (for phone-registered customers)
    public static final String TWILIO_ACCOUNT_SID = "YOUR_TWILIO_ACCOUNT_SID";
    public static final String TWILIO_AUTH_TOKEN = "YOUR_TWILIO_AUTH_TOKEN";
    public static final String TWILIO_FROM_NUMBER = "YOUR_TWILIO_FROM_NUMBER";
}
