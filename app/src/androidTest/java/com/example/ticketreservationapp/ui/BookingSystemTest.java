package com.example.ticketreservationapp.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BookingSystemTest {

    @Before
    public void setUp() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), BookingActivity.class);

        intent.putExtra("eventID", "dummy_test_event_123");

        ActivityScenario.launch(intent);
    }

    @Test
    public void testBookingUI_validatesEmptyQuantity() {
        onView(withId(R.id.btnConfirmBooking)).perform(click());

        onView(withId(R.id.btnConfirmBooking)).check(matches(isDisplayed()));
    }

    @Test
    public void testBookingUI_entersDataAndSubmits() {
        onView(withId(R.id.etTicketCount))
                .perform(typeText("2"), closeSoftKeyboard());

        onView(withId(R.id.etTicketCount)).check(matches(withText("2")));

        onView(withId(R.id.btnConfirmBooking)).perform(click());
    }
}