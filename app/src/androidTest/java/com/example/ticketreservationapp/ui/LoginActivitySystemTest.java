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
public class LoginActivitySystemTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testNavigateToRegisterScreen() {
        onView(withId(R.id.tvRegisterRedirect)).perform(click());

        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginWithEmailAndPassword() {
        onView(withId(R.id.etLoginEmail))
                .perform(typeText("testuser@uniters.com"), closeSoftKeyboard());

        onView(withId(R.id.etLoginPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btnLogin)).perform(click());

    }
}