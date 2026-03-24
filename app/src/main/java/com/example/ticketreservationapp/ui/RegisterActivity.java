package com.example.ticketreservationapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.utils.AuthValidator;
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
    private RadioGroup rgRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole); // Bind the new radio group

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

        //role selection
        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRoleId);
        String role = selectedRadioButton.getText().toString(); //"customer" or "administrator"

        //email & password registration
        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
            //uses validator in utils
            if (!AuthValidator.isValidEmail(email)) {
                etEmail.setError("Please enter a valid email.");
                return;
            }
            if (!AuthValidator.isValidPassword(password)) {
                etPassword.setError("Password must be at least 6 characters.");
                return;
            }

            String formattedPhone = AuthValidator.formatPhoneNumber(phone);
            registerWithEmail(email, password, formattedPhone, role);
        }
        //phone registration
        else if (!TextUtils.isEmpty(phone)) {
            String formattedPhone = AuthValidator.formatPhoneNumber(phone);
            sendPhoneVerification(formattedPhone, role);
        }
        //nothing provided
        else {
            Toast.makeText(this, "Please provide an Email/Password OR a Phone Number", Toast.LENGTH_LONG).show();
        }
    }

    private void registerWithEmail(String email, String password, String phone, String role) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(email, phone, role);
                    } else {
                        showErrorMsg(task.getException());
                    }
                });
    }

    private void sendPhoneVerification(String phone, String role) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential, phone, role);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(RegisterActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        RegisterActivity.this.verificationId = verificationId;
                        showOTPDialog(phone, role);
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOTPDialog(String phone, String role) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Verification Code");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString();
            if (!TextUtils.isEmpty(code)) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithPhoneAuthCredential(credential, phone, role);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String phone, String role) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore("", phone, role);
                    } else {
                        showErrorMsg(task.getException());
                    }
                });
    }

    //saves role to db
    private void saveUserToFirestore(String email, String phone, String role) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("phoneNumber", phone);
            userData.put("role", role); //customer or admin

            db.collection("Users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> showSuccessDialog())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show());
        }
    }

    private void showSuccessDialog() {
        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
        mAuth.signOut();
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