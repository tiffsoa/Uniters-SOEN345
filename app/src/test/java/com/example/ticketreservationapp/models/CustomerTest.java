package com.example.ticketreservationapp.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    @Test
    public void constructor_validInput_shouldInitializeFields() {
        String expectedId = "user_123";
        String expectedEmail = "test@uniters.com";
        String expectedPhone = "5551234567";

        Customer testCustomer = new Customer(expectedId, expectedEmail, expectedPhone);

        assertEquals(expectedId, testCustomer.getUserID(), "User ID should match the input");
        assertEquals(expectedEmail, testCustomer.getEmail(), "Email should match the input");
        assertEquals(expectedPhone, testCustomer.getPhoneNumber(), "Phone number should match the input");
    }
}
