package com.example.ticketreservationapp.utils;
import java.text.SimpleDateFormat;
import java.util.Date;
//utility that converts date string to ensure consistency
public class DateParser {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public static Date parse(String input) throws Exception {
        return FORMAT.parse(input);
    }
}