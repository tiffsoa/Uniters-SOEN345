package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.BuildConfig;

// Credentials are injected at build time from local.properties via BuildConfig.
// See local.properties.example at the repo root for the required keys.
public class NotificationConfig {

    // Brevo REST API (for email-registered customers)
    public static final String BREVO_API_KEY = BuildConfig.BREVO_API_KEY;
    public static final String SENDER_EMAIL = BuildConfig.BREVO_SENDER_EMAIL;
    public static final String SENDER_NAME = BuildConfig.BREVO_SENDER_NAME;

    // Twilio REST API (for phone-registered customers)
    public static final String TWILIO_ACCOUNT_SID = BuildConfig.TWILIO_ACCOUNT_SID;
    public static final String TWILIO_AUTH_TOKEN = BuildConfig.TWILIO_AUTH_TOKEN;
    public static final String TWILIO_FROM_NUMBER = BuildConfig.TWILIO_FROM_NUMBER;
}
