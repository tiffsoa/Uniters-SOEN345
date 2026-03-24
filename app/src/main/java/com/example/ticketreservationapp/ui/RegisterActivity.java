package com.example.ticketreservationapp.ui;
import android.content.Intent;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPhone, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String verificationId; // Needed for phone auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true); // Bypasses reCAPTCHA
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginRedirect = findViewById(R.id.tvLoginRedirect);

        btnRegister.setOnClickListener(v -> handleRegistration());

        tvLoginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // Branch 1: Email & Password Registration
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters.");
                return;
            }
            registerWithEmail(email, password, phone);
        }
        // Branch 2: Phone Registration (Email is empty, Phone is not)
        else if (!TextUtils.isEmpty(phone)) {
            // Note: Firebase requires the country code (e.g., +15551234567)
            if (!phone.startsWith("+")) {
                phone = "+1" + phone; // Default to North America if no country code provided
            }
            sendPhoneVerification(phone);
        }
        // Branch 3: Nothing provided
        else {
            Toast.makeText(this, "Please provide an Email/Password OR a Phone Number", Toast.LENGTH_LONG).show();
        }
    }

    private void registerWithEmail(String email, String password, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(email, phone);
                    } else {
                        showErrorMsg(task.getException());
                    }
                });
    }

    private void sendPhoneVerification(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Sometimes auto-resolves on emulator
                        signInWithPhoneAuthCredential(credential, phone);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(RegisterActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        RegisterActivity.this.verificationId = verificationId;
                        showOTPDialog(phone);
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOTPDialog(String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Verification Code");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString();
            if (!TextUtils.isEmpty(code)) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithPhoneAuthCredential(credential, phone);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String phone) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore("", phone); // Email is blank for phone-only users
                    } else {
                        showErrorMsg(task.getException());
                    }
                });
    }

    private void saveUserToFirestore(String email, String phone) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("phoneNumber", phone);
            userData.put("role", "Customer");

            db.collection("Users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> showSuccessDialog())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show());
        }
    }

    private void showSuccessDialog() {
        // Force a Toast message instead of a popup
        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();

        mAuth.signOut(); // Wipes the session
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void showErrorMsg(Exception e) {
        String errorMsg = e != null ? e.getMessage() : "Unknown error";
        Toast.makeText(this, "Authentication failed: " + errorMsg, Toast.LENGTH_LONG).show();
        System.out.println(errorMsg);
    }
}