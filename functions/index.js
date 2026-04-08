// ---
// Firebase cloud functions for handling ticket reservations and cancellations
// Firestore Schema:
//  - Events/{eventID}         : name, date (Timestamp), location, category, maxCapacity, bookedSeats, isCancelled (bool)
//  - Reservations/{resID}     : eventID, customerID, ticketCount, isCancelled (bool), createdAt (Timestamp)
//  - Users/{uid}              : email, phoneNumber, role, authMethod
// remainingCapacity is inferred on the client: maxCapacity - bookedSeats
// ---

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue, Timestamp } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

// makeReservation: callable cloud function
// Creates a reservation document and increments bookedSeats on the event
// Uses a firestore transaction to prevent double-booking
exports.makeReservation = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "You must be signed in to make a reservation.");
  }

  const customerID = request.auth.uid;
  const { eventID, ticketCount } = request.data;

  if (!eventID || typeof eventID !== "string") {
    throw new HttpsError("invalid-argument", "eventID is required and must be a string.");
  }
  if (!ticketCount || typeof ticketCount !== "number" || ticketCount < 1 || ticketCount > 8) {
    throw new HttpsError("invalid-argument", "ticketCount must be a number between 1 and 8.");
  }

  const eventRef = db.collection("Events").doc(eventID);

  const result = await db.runTransaction(async (transaction) => {
    const eventSnap = await transaction.get(eventRef);

    if (!eventSnap.exists) {
      throw new HttpsError("not-found", "Event does not exist.");
    }

    const eventData = eventSnap.data();

    if (eventData.isCancelled === true) {
      throw new HttpsError("failed-precondition", "This event has been cancelled.");
    }

    const remaining = eventData.maxCapacity - (eventData.bookedSeats || 0);
    if (ticketCount > remaining) {
      throw new HttpsError(
        "resource-exhausted",
        `Not enough seats. Only ${remaining} remaining.`
      );
    }

    // Create reservation document
    const reservationRef = db.collection("Reservations").doc();
    const reservationID = reservationRef.id;

    transaction.set(reservationRef, {
      reservationID: reservationID,
      eventID: eventID,
      customerID: customerID,
      ticketCount: ticketCount,
      isCancelled: false,
      createdAt: Timestamp.now(),
    });

    // Increment bookedSeats on the event
    transaction.update(eventRef, {
      bookedSeats: FieldValue.increment(ticketCount),
    });

    return {
      reservationID: reservationID,
      eventName: eventData.name,
    };
  });

  console.log(
    `[NOTIFICATION] Reservation ${result.reservationID} confirmed for customer ${customerID}. ` +
    `Event: ${result.eventName}, Tickets: ${ticketCount}`
  );

  return {
    success: true,
    reservationID: result.reservationID,
    message: `Successfully reserved ${ticketCount} ticket(s) for ${result.eventName}.`,
  };
});

// cancelReservation: callable cloud function
// Marks a reservation as cancelled and returns ticket capacity to the event
// Uses a firestore transaction for consistency
exports.cancelReservation = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "You must be signed in to cancel a reservation.");
  }

  const customerID = request.auth.uid;
  const { reservationID } = request.data;

  if (!reservationID || typeof reservationID !== "string") {
    throw new HttpsError("invalid-argument", "reservationID is required and must be a string.");
  }

  const reservationRef = db.collection("Reservations").doc(reservationID);

  const result = await db.runTransaction(async (transaction) => {
    const resSnap = await transaction.get(reservationRef);

    if (!resSnap.exists) {
      throw new HttpsError("not-found", "Reservation does not exist.");
    }

    const resData = resSnap.data();

    // Ownership check
    if (resData.customerID !== customerID) {
      throw new HttpsError("permission-denied", "You can only cancel your own reservations.");
    }

    if (resData.isCancelled === true) {
      throw new HttpsError("failed-precondition", "This reservation is already cancelled.");
    }

    const eventRef = db.collection("Events").doc(resData.eventID);
    const eventSnap = await transaction.get(eventRef);

    // Mark reservation as cancelled
    transaction.update(reservationRef, {
      isCancelled: true,
    });

    // Return tickets to event capacity
    if (eventSnap.exists) {
      transaction.update(eventRef, {
        bookedSeats: FieldValue.increment(-resData.ticketCount),
      });
    }

    return {
      reservationID: reservationID,
      ticketCount: resData.ticketCount,
    };
  });

  console.log(
    `[NOTIFICATION] Reservation ${result.reservationID} cancelled for customer ${customerID}. ` +
    `Tickets returned: ${result.ticketCount}`
  );

  return {
    success: true,
    reservationID: result.reservationID,
    message: `Reservation cancelled. ${result.ticketCount} ticket(s) returned to event capacity.`,
  };
});

// seedTestEvent: utility function for testing without admin UI
exports.seedTestEvent = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "You must be signed in.");
  }

  const { name, date, location, category, maxCapacity } = request.data;

  if (!name || !date || !location || !category || !maxCapacity) {
    throw new HttpsError("invalid-argument", "All event fields are required: name, date, location, category, maxCapacity.");
  }

  const eventRef = db.collection("Events").doc();
  const eventData = {
    eventID: eventRef.id,
    name: name,
    date: Timestamp.fromDate(new Date(date)),
    location: location,
    category: category,
    maxCapacity: maxCapacity,
    bookedSeats: 0,
    isCancelled: false,
  };

  await eventRef.set(eventData);

  return {
    success: true,
    eventID: eventRef.id,
    message: `Test event '${name}' created successfully.`,
  };
});
