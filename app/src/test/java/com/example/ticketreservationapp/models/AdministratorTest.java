package com.example.ticketreservationapp.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdministratorTest {

    @Test
    public void constructor_validInput_shouldInitializeFields() {
        String expectedId = "admin_999";
        String expectedEmail = "admin@system.com";
        String expectedPhone = "+15559990000";

        Administrator testAdmin = new Administrator(expectedId, expectedEmail, expectedPhone);

        assertEquals(expectedId, testAdmin.getUserID(), "Admin ID should match the input");
        assertEquals(expectedEmail, testAdmin.getEmail(), "Admin Email should match the input");
        assertEquals(expectedPhone, testAdmin.getPhoneNumber(), "Admin Phone should match the input");
    }

}