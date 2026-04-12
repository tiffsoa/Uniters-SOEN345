package com.example.ticketreservationapp.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTest {

    @Test
    void validInput_shouldPass() {
        assertDoesNotThrow(() ->
                InputValidator.validate(
                        "Orchestra Concert",
                        "Place Bell, Montreal",
                        "Music",
                        "11000",
                        "10/12/2026"
                )
        );
    }

    @Test
    void emptyName_shouldThrow() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                InputValidator.validate(
                        "",
                        "Place Bell, Montreal",
                        "Music",
                        "11000",
                        "10/12/2026"
                )
        );

        assertEquals("Name is required", e.getMessage());
    }

    @Test
    void nullLocation_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                InputValidator.validate(
                        "A",
                        null,
                        "Music",
                        "11000",
                        "10/12/2026"
                )
        );
    }

    @Test
    void emptyCapacity_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                InputValidator.validate(
                        "A",
                        "B",
                        "C",
                        "",
                        "10/12/2026"
                )
        );
    }
}