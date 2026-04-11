package com.example.ticketreservationapp.utils;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class DateParserTest {
    @Test
    void parse_validDate_shouldReturnDate() throws Exception {
        Date d = DateParser.parse("10/12/2026");
        assertNotNull(d);
    }

    @Test
    void parse_invalidDate_shouldThrow() {
        assertThrows(Exception.class, () -> DateParser.parse("invalid"));
    }
}