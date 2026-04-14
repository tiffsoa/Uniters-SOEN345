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
public class CancellationIntegrationTest {

    private FirebaseFirestore db;
    private ReservationRepository repository;
    private CollectionReference testEventsRef;
    private CollectionReference testReservationsRef;

    private final String TEST_EVENT_ID = "cancel_test_event_1";
    private final String TEST_RES_ID = "cancel_test_res_1";

    @Before
    public void setUp() throws InterruptedException {
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().getTargetContext());
        db = FirebaseFirestore.getInstance();
        testEventsRef = db.collection("TestEvents");
        testReservationsRef = db.collection("TestReservations");

        repository = new ReservationRepository(testReservationsRef, testEventsRef);


        Event event = new Event(TEST_EVENT_ID, "Cancellation Fest", new Date(), "MTL", "Music", 100);
        event.setBookedSeats(5);


        Reservation reservation = new Reservation(TEST_RES_ID, TEST_EVENT_ID, "customer_1", 5);
        reservation.setCancelled(false);

        CountDownLatch setupLatch = new CountDownLatch(2);
        testEventsRef.document(TEST_EVENT_ID).set(event).addOnCompleteListener(task -> setupLatch.countDown());
        testReservationsRef.document(TEST_RES_ID).set(reservation).addOnCompleteListener(task -> setupLatch.countDown());
        setupLatch.await(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        testEventsRef.document(TEST_EVENT_ID).delete();
        testReservationsRef.document(TEST_RES_ID).delete();
    }

    @Test
    public void testAtomicCancel_restoresCapacity() throws InterruptedException {
        CountDownLatch transactionLatch = new CountDownLatch(1);

        Reservation reservationToCancel = new Reservation(TEST_RES_ID, TEST_EVENT_ID, "customer_1", 5);

        repository.cancel(reservationToCancel,
                () -> transactionLatch.countDown(),
                error -> fail("Transaction failed: " + error)
        );

        assertTrue("Timeout on cancel transaction", transactionLatch.await(10, TimeUnit.SECONDS));

        CountDownLatch verifyLatch = new CountDownLatch(2);


        testEventsRef.document(TEST_EVENT_ID).get().addOnSuccessListener(doc -> {
            assertEquals(Long.valueOf(0), doc.getLong("bookedSeats"));
            verifyLatch.countDown();
        });


        testReservationsRef.document(TEST_RES_ID).get().addOnSuccessListener(doc -> {
            assertTrue(doc.getBoolean("isCancelled"));
            verifyLatch.countDown();
        });

        assertTrue("Timeout verifying data", verifyLatch.await(5, TimeUnit.SECONDS));
    }
}