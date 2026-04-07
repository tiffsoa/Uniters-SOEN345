// ---
// Seed script for populating test events in firestore emulator
// Usage:
//   1. Start emulators: firebase emulators:start
//   2. Run this script: node functions/seed-events.js
// This creates sample events so you can test Reservations & Cancellations without depending on the Admin UI PR.
// ---

const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

// Point to the firestore emulator
process.env.FIRESTORE_EMULATOR_HOST = "localhost:8080";

initializeApp({ projectId: "ticketreservationapp-soen345" });
const db = getFirestore();

const testEvents = [
  {
    name: "Summer Music Festival 2026",
    date: "2026-07-15T18:00:00",
    location: "Montreal Olympic Stadium",
    category: "Music",
    capacity: 500,
    bookedSeats: 0,
    status: "active",
  },
  {
    name: "Tech Conference 2026",
    date: "2026-08-20T09:00:00",
    location: "Palais des congrès de Montréal",
    category: "Technology",
    capacity: 200,
    bookedSeats: 0,
    status: "active",
  },
  {
    name: "Basketball Championship",
    date: "2026-09-10T19:30:00",
    location: "Centre Bell",
    category: "Sports",
    capacity: 100,
    bookedSeats: 0,
    status: "active",
  },
  {
    name: "Stand-Up Comedy Night",
    date: "2026-06-01T20:00:00",
    location: "Theatre St-Denis",
    category: "Entertainment",
    capacity: 50,
    bookedSeats: 0,
    status: "active",
  },
  {
    name: "Almost Sold Out Show",
    date: "2026-05-15T21:00:00",
    location: "Small Venue Downtown",
    category: "Music",
    capacity: 5,
    bookedSeats: 3,
    status: "active",
  },
];

async function seedEvents() {
  console.log("Seeding test events into Firestore emulator...\n");

  for (const event of testEvents) {
    const docRef = db.collection("Events").doc();
    event.eventID = docRef.id;
    await docRef.set(event);
    console.log(`Created: "${event.name}" (ID: ${event.eventID})`);
  }

  console.log(`\nDone! ${testEvents.length} events seeded.`);
  process.exit(0);
}

seedEvents().catch((err) => {
  console.error("Error seeding events:", err);
  process.exit(1);
});
