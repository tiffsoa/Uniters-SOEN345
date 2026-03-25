package com.example.ticketreservationapp.utils;

import android.util.Patterns;

public class AuthValidator {

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }

    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        String cleanedPhone = phone.trim();
        if (!cleanedPhone.startsWith("+")) {
            return "+1" + cleanedPhone;
        }
        return cleanedPhone;
    }
}