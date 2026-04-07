// ---
// Firebase cloud functions for handling ticket reservations and cancellations
// Firestore Schema:
//  - Events/{eventID}     : name, date, location, category, capacity, bookedSeats, status
//  - Reservations/{resID} : eventID, customerID, quantity, status, eventName, eventDate, eventLocation, ticketIDs[], createdAt
//  - Tickets/{ticketID}   : reservationID, eventID, customerID, status, issuedAt
//  - Users/{uid}          : email, phoneNumber, role, authMethod
// ---

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

//  makeReservation: callable Cloud Function
exports.makeReservation = onCall(async (request) => {
  // Auth check
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "You must be signed in to make a reservation.");
  }

  const customerID = request.auth.uid;
  const { eventID, quantity } = request.data;

  // Input validation
  if (!eventID || typeof eventID !== "string") {
    throw new HttpsError("invalid-argument", "eventID is required and must be a string.");
  }
  if (!quantity || typeof quantity !== "number" || quantity < 1 || quantity > 10) {
    throw new HttpsError("invalid-argument", "quantity must be a number between 1 and 10.");
  }

  const eventRef = db.collection("Events").doc(eventID);

  // Firestore transaction (prevents double-booking)
  const result = await db.runTransaction(async (transaction) => {
    const eventSnap = await transaction.get(eventRef);

    if (!eventSnap.exists) {
      throw new HttpsError("not-found", "Event does not exist.");
    }

    const eventData = eventSnap.data();

    if (eventData.status === "canceled") {
      throw new HttpsError("failed-precondition", "This event has been canceled.");
    }

    const remaining = eventData.capacity - (eventData.bookedSeats || 0);
    if (quantity > remaining) {
      throw new HttpsError(
        "resource-exhausted",
        `Not enough seats. Only ${remaining} remaining.`
      );
    }

    // Create Reservation document
    const reservationRef = db.collection("Reservations").doc();
    const reservationID = reservationRef.id;

    // Create Ticket documents
    const ticketIDs = [];
    for (let i = 0; i < quantity; i++) {
      const ticketRef = db.collection("Tickets").doc();
      ticketIDs.push(ticketRef.id);
      transaction.set(ticketRef, {
        ticketID: ticketRef.id,
        reservationID: reservationID,
        eventID: eventID,
        customerID: customerID,
        status: "valid",
        issuedAt: Date.now(),
      });
    }

    // Write reservation
    transaction.set(reservationRef, {
      reservationID: reservationID,
      eventID: eventID,
      customerID: customerID,
      quantity: quantity,
      status: "confirmed",
      eventName: eventData.name || "",
      eventDate: eventData.date || "",
      eventLocation: eventData.location || "",
      ticketIDs: ticketIDs,
      createdAt: Date.now(),
    });

    // Increment bookedSeats
    transaction.update(eventRef, {
      bookedSeats: FieldValue.increment(quantity),
    });

    return {
      reservationID: reservationID,
      ticketIDs: ticketIDs,
      eventName: eventData.name,
    };
  });

  // Notification stub (replace with real email/SMS integration)
  console.log(
    `[NOTIFICATION] Reservation ${result.reservationID} confirmed for customer ${customerID}. ` +
    `Event: ${result.eventName}, Tickets: ${result.ticketIDs.length}`
  );

  return {
    success: true,
    reservationID: result.reservationID,
    ticketIDs: result.ticketIDs,
    message: `Successfully reserved ${quantity} ticket(s) for ${result.eventName}.`,
  };
});

// cancelReservation: callable cloud function
exports.cancelReservation = onCall(async (request) => {
  // Auth check
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

    if (resData.status === "canceled") {
      throw new HttpsError("failed-precondition", "This reservation is already canceled.");
    }

    // Read the event to validate it exists
    const eventRef = db.collection("Events").doc(resData.eventID);
    const eventSnap = await transaction.get(eventRef);

    // Mark reservation as canceled
    transaction.update(reservationRef, {
      status: "canceled",
    });

    // Mark all tickets as canceled
    const ticketIDs = resData.ticketIDs || [];
    for (const ticketID of ticketIDs) {
      const ticketRef = db.collection("Tickets").doc(ticketID);
      transaction.update(ticketRef, {
        status: "canceled",
      });
    }

    // Decrement bookedSeats
    if (eventSnap.exists) {
      transaction.update(eventRef, {
        bookedSeats: FieldValue.increment(-resData.quantity),
      });
    }

    return {
      reservationID: reservationID,
      eventName: resData.eventName || "",
      quantity: resData.quantity,
    };
  });

  // Notification stub: replace with real email/SMS integration
  console.log(
    `[NOTIFICATION] Reservation ${result.reservationID} canceled for customer ${customerID}. ` +
    `Event: ${result.eventName}, Tickets refunded: ${result.quantity}`
  );

  return {
    success: true,
    reservationID: result.reservationID,
    message: `Reservation for ${result.eventName} has been canceled. ${result.quantity} ticket(s) released.`,
  };
});

// seedTestEvent: utility function for testing without admin UI
exports.seedTestEvent = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "You must be signed in.");
  }

  const { name, date, location, category, capacity } = request.data;

  if (!name || !date || !location || !category || !capacity) {
    throw new HttpsError("invalid-argument", "All event fields are required: name, date, location, category, capacity.");
  }

  const eventRef = db.collection("Events").doc();
  const eventData = {
    eventID: eventRef.id,
    name: name,
    date: date,
    location: location,
    category: category,
    capacity: capacity,
    bookedSeats: 0,
    status: "active",
  };

  await eventRef.set(eventData);

  return {
    success: true,
    eventID: eventRef.id,
    message: `Test event '${name}' created successfully.`,
  };
});
