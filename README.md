# Uniters - Ticket Reservation Application

A cloud-based Android application for booking and managing event tickets, developed for SOEN 345 (Software Testing, Verification and Quality Assurance). This application utilizes a serverless architecture powered by Firebase Firestore and Firebase Authentication.

## Prerequisites
* **Android Studio** (Latest version recommended)
* **Java Development Kit (JDK) 11** or higher
* An Android Emulator or physical device running Android 8.0 (API 26) or higher.

## Setup and Installation

**1. Clone the repository:**
```bash
git clone [https://github.com/YOUR-USERNAME/Uniters-SOEN345.git](https://github.com/YOUR-USERNAME/Uniters-SOEN345.git)
```

**2. Open the project:**
Open Android Studio, select "Open", and navigate to the folder where you cloned the repository. Let Gradle sync completely.

**3. Configure API Keys (Crucial Step):**
To enable email and SMS notifications for ticket bookings, you must provide your own API keys for Brevo and Twilio.

In the root directory of the project (at the same level as `app/` and `build.gradle.kts`), create or open the file named `local.properties`. Add the following lines, replacing the placeholders with your actual API credentials:
```bash
brevo.apiKey=YOUR_BREVO_API_KEY
brevo.senderEmail=unitersmobileapps@gmail.com
brevo.senderName=Uniters Tickets
twilio.accountSid=YOUR_TWILIO_ACCOUNT_SID
twilio.authToken=YOUR_TWILIO_AUTH_TOKEN
twilio.fromNumber=YOUR_TWILIO_PHONE_NUMBER
```

**4. Run the Application:**
Click the Run 'app' button (the green play arrow) in the top toolbar of Android Studio to launch the application on your emulator or plugged-in device.

## Testing
This project contains a comprehensive suite of Unit, Integration, and System tests (via Espresso).

To run the Unit Tests: Right-click the `app/src/test/java/` folder and select "Run Tests".

To run the Integration & System Tests: Ensure your emulator is running, right-click the `app/src/androidTest/java/` folder, and select "Run Tests".
