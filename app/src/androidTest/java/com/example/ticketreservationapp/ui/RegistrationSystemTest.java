package com.example.ticketreservationapp.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ticketreservationapp.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RegistrationSystemTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityRule =
            new ActivityScenarioRule<>(RegisterActivity.class);

    @Test
    public void testRegistrationUI_blocksEmptySubmission() {
        onView(withId(R.id.btnRegister)).perform(click());

        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }

    @Test
    public void testRegistrationUI_entersDataAndSubmits() {
        onView(withId(R.id.etEmail))
                .perform(typeText("newuser@uniters.com"), closeSoftKeyboard());

        onView(withId(R.id.etPassword))
                .perform(typeText("Password123!"), closeSoftKeyboard());

        onView(withId(R.id.rbCustomer)).perform(click());

        onView(withId(R.id.btnRegister)).perform(click());
    }

    @Test
    public void testRegistrationUI_redirectsToLogin() {
        onView(withId(R.id.tvLoginRedirect)).perform(click());
    }
}