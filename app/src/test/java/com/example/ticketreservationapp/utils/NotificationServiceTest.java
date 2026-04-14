package com.example.ticketreservationapp.utils;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationServiceTest {

    // Testable subclass that intercepts send calls instead of making real HTTP requests
    static class SpyNotificationService extends NotificationService {
        boolean emailCalled = false;
        boolean smsCalled = false;
        String lastEmailTarget = null;
        String lastSmsTarget = null;

        @Override
        void sendEmailAsync(String toEmail, Event event, Reservation reservation) {
            emailCalled = true;
            lastEmailTarget = toEmail;
        }

        @Override
        void sendSmsAsync(String toPhone, Event event, Reservation reservation) {
            smsCalled = true;
            lastSmsTarget = toPhone;
        }
    }

    private SpyNotificationService service;
    private Event event;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        service = new SpyNotificationService();
        event = new Event("e1", "Jazz Night", new Date(1800000000000L), "Civic Center", "Music", 100);
        reservation = new Reservation("r1", "e1", "c1", 2);
    }

    // Routing tests

    @Test
    void sendBookingConfirmation_emailProvided_shouldRouteToEmail() {
        service.sendBookingConfirmation("user@test.com", null, event, reservation);

        assertTrue(service.emailCalled);
        assertFalse(service.smsCalled);
        assertEquals("user@test.com", service.lastEmailTarget);
    }

    @Test
    void sendBookingConfirmation_phoneProvided_shouldRouteToSms() {
        service.sendBookingConfirmation(null, "+15141234567", event, reservation);

        assertTrue(service.smsCalled);
        assertFalse(service.emailCalled);
        assertEquals("+15141234567", service.lastSmsTarget);
    }

    @Test
    void sendBookingConfirmation_emailAndPhone_shouldPreferEmail() {
        service.sendBookingConfirmation("user@test.com", "+15141234567", event, reservation);

        assertTrue(service.emailCalled);
        assertFalse(service.smsCalled);
    }

    @Test
    void sendBookingConfirmation_emptyEmail_shouldRouteToSms() {
        service.sendBookingConfirmation("", "+15141234567", event, reservation);

        assertTrue(service.smsCalled);
        assertFalse(service.emailCalled);
    }

    @Test
    void sendBookingConfirmation_neitherEmailNorPhone_shouldSendNothing() {
        service.sendBookingConfirmation(null, null, event, reservation);

        assertFalse(service.emailCalled);
        assertFalse(service.smsCalled);
    }

    // buildMessageBody tests

    @Test
    void buildMessageBody_shouldContainEventName() {
        String body = service.buildMessageBody(event, reservation);
        assertTrue(body.contains("Jazz Night"));
    }

    @Test
    void buildMessageBody_shouldContainEventLocation() {
        String body = service.buildMessageBody(event, reservation);
        assertTrue(body.contains("Civic Center"));
    }

    @Test
    void buildMessageBody_shouldContainEventCategory() {
        String body = service.buildMessageBody(event, reservation);
        assertTrue(body.contains("Music"));
    }

    @Test
    void buildMessageBody_shouldContainTicketCount() {
        String body = service.buildMessageBody(event, reservation);
        assertTrue(body.contains("2"));
    }

    @Test
    void buildMessageBody_shouldContainReservationID() {
        String body = service.buildMessageBody(event, reservation);
        assertTrue(body.contains("r1"));
    }

    @Test
    void buildMessageBody_shouldContainGreetingAndClosing() {
        String body = service.buildMessageBody(event, reservation);
        assertTrue(body.contains("Hello,"));
        assertTrue(body.contains("Thank you for booking with Uniters!"));
    }

    // escapeJson tests

    @Test
    void escapeJson_plainText_shouldReturnUnchanged() {
        assertEquals("hello world", service.escapeJson("hello world"));
    }

    @Test
    void escapeJson_doubleQuotes_shouldBeEscaped() {
        assertEquals("say \\\"hi\\\"", service.escapeJson("say \"hi\""));
    }

    @Test
    void escapeJson_backslash_shouldBeEscaped() {
        assertEquals("a\\\\b", service.escapeJson("a\\b"));
    }

    @Test
    void escapeJson_newline_shouldBeEscaped() {
        assertEquals("line1\\nline2", service.escapeJson("line1\nline2"));
    }

    @Test
    void escapeJson_combinedSpecialChars_shouldAllBeEscaped() {
        String result = service.escapeJson("\"name\"\nvalue\\end");
        assertEquals("\\\"name\\\"\\nvalue\\\\end", result);
    }
}
