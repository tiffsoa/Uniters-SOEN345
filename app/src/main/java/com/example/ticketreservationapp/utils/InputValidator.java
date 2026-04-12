package com.example.ticketreservationapp.utils;
//explicitly for validating user input
public class InputValidator {
    public static void validate(String name,
                                String location,
                                String category,
                                String capacity,
                                String date) {

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name is required");

        if (location == null || location.trim().isEmpty())
            throw new IllegalArgumentException("Location is required");

        if (category == null || category.trim().isEmpty())
            throw new IllegalArgumentException("Category is required");

        if (capacity == null || capacity.trim().isEmpty())
            throw new IllegalArgumentException("Capacity is required");

        if (date == null || date.trim().isEmpty())
            throw new IllegalArgumentException("Date is required");
    }
}