package com.example.ticketreservationapp.models;

import com.example.ticketreservationapp.utils.AuthValidator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthValidatorTest {

    @Test
    public void testValidEmail() {
        assertTrue(AuthValidator.isValidEmail("test@uniters.com"), "Standard email should be valid");
        assertTrue(AuthValidator.isValidEmail("user.name@domain.co"), "Email with dot should be valid");
    }

    @Test
    public void testInvalidEmail() {
        assertFalse(AuthValidator.isValidEmail(""), "Empty email should be invalid");
        assertFalse(AuthValidator.isValidEmail("test@.com"), "Email missing domain should be invalid");
        assertFalse(AuthValidator.isValidEmail("test.com"), "Email missing @ should be invalid");
        assertFalse(AuthValidator.isValidEmail(null), "Null email should be invalid");
    }

    @Test
    public void testValidPassword() {
        assertTrue(AuthValidator.isValidPassword("123456"), "6 character password should be valid");
        assertTrue(AuthValidator.isValidPassword("StrongPass123!"), "Long password should be valid");
    }

    @Test
    public void testInvalidPassword() {
        assertFalse(AuthValidator.isValidPassword("12345"), "5 character password should be invalid");
        assertFalse(AuthValidator.isValidPassword(""), "Empty password should be invalid");
        assertFalse(AuthValidator.isValidPassword(null), "Null password should be invalid");
    }

    @Test
    public void testFormatPhoneNumberWithoutPrefix() {
        String rawPhone = "5551234567";
        String expected = "+15551234567";
        assertEquals(expected, AuthValidator.formatPhoneNumber(rawPhone), "Should prepend +1 to raw number");
    }

    @Test
    public void testFormatPhoneNumberWithPrefix() {
        String rawPhone = "+15551234567";
        assertEquals(rawPhone, AuthValidator.formatPhoneNumber(rawPhone), "Should not alter already prefixed number");
    }
}
