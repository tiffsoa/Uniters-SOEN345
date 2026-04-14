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
        // 1. Create a custom Intent targeting your BookingActivity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), BookingActivity.class);

        // 2. Add the required eventID extra so the Activity doesn't immediately finish()
        intent.putExtra("eventID", "dummy_test_event_123");

        // 3. Manually launch the Activity for the test
        ActivityScenario.launch(intent);
    }

    @Test
    public void testBookingUI_validatesEmptyQuantity() {
        // Find the confirm button and click it without entering a quantity
        onView(withId(R.id.btnConfirmBooking)).perform(click());

        // Check if the button is still displayed (meaning we didn't navigate away due to validation failing)
        onView(withId(R.id.btnConfirmBooking)).check(matches(isDisplayed()));
    }

    @Test
    public void testBookingUI_entersDataAndSubmits() {
        // Type "2" into the ticket quantity input
        onView(withId(R.id.etTicketCount))
                .perform(typeText("2"), closeSoftKeyboard());

        // Verify the text was entered
        onView(withId(R.id.etTicketCount)).check(matches(withText("2")));

        // Click the confirm button
        onView(withId(R.id.btnConfirmBooking)).perform(click());
    }
}