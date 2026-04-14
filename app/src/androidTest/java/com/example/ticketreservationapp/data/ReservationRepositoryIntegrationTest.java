package com.example.ticketreservationapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.ticketreservationapp.models.Event;
import com.example.ticketreservationapp.models.Reservation;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ReservationRepositoryIntegrationTest {

    private FirebaseFirestore db;
    private ReservationRepository reservationRepository;
    private CollectionReference testEventsRef;
    private CollectionReference testReservationsRef;

    private final String TEST_EVENT_ID = "test_event_integration_1";
    private final String TEST_RES_ID = "test_res_integration_1";

    @Before
    public void setUp() throws InterruptedException {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getTargetContext());

        db = FirebaseFirestore.getInstance();
        testEventsRef = db.collection("TestEvents");
        testReservationsRef = db.collection("TestReservations");

        reservationRepository = new ReservationRepository(testReservationsRef, testEventsRef);

        Event testEvent = new Event(TEST_EVENT_ID, "Integration Fest", new Date(), "MTL", "Music", 100);

        CountDownLatch setupLatch = new CountDownLatch(1);
        testEventsRef.document(TEST_EVENT_ID).set(testEvent).addOnCompleteListener(task -> setupLatch.countDown());

        setupLatch.await(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        testEventsRef.document(TEST_EVENT_ID).delete();
        testReservationsRef.document(TEST_RES_ID).delete();
    }

    @Test
    public void testAtomicBooking_decrementsCapacity() throws InterruptedException {
        CountDownLatch transactionLatch = new CountDownLatch(1);

        Reservation reservation = new Reservation(TEST_RES_ID, TEST_EVENT_ID, "test_customer", 5);

        reservationRepository.book(reservation,
                () -> transactionLatch.countDown(), // On Success
                error -> fail("Transaction failed: " + error) // On Error
        );

        assertTrue("Timeout waiting for Firestore transaction", transactionLatch.await(10, TimeUnit.SECONDS));

        CountDownLatch verifyLatch = new CountDownLatch(1);
        testEventsRef.document(TEST_EVENT_ID).get().addOnSuccessListener(doc -> {
            Long bookedSeats = doc.getLong("bookedSeats");
            assertEquals("Booked seats should now be 5", Long.valueOf(5), bookedSeats);
            verifyLatch.countDown();
        });

        assertTrue("Timeout verifying Firestore data", verifyLatch.await(5, TimeUnit.SECONDS));
    }
}