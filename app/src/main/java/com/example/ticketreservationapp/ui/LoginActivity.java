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
import androidx.lifecycle.ViewModelProvider;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.utils.AuthValidator;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail, etLoginPassword, etLoginPhone;
    private FirebaseAuth mAuth;
    private Button btnLogin;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        mAuth = FirebaseAuth.getInstance();

        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        etLoginPhone = findViewById(R.id.etLoginPhone);

        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterRedirect = findViewById(R.id.tvRegisterRedirect);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser() {
        btnLogin.setEnabled(false);
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();
        String phone = etLoginPhone != null ? etLoginPhone.getText().toString().trim() : "";

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            loginWithEmail(email, password);
        }
        else if (!TextUtils.isEmpty(phone)) {
            String formattedPhone = AuthValidator.formatPhoneNumber(phone);
            sendPhoneVerification(formattedPhone);
        }
        else {
            Toast.makeText(this, "Please enter Email & Password OR a Phone Number.", Toast.LENGTH_LONG).show();
            btnLogin.setEnabled(true);
        }
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleSuccessfulLogin();
                    } else {
                        showErrorMsg(task.getException());
                        btnLogin.setEnabled(true);
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
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(LoginActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnLogin.setEnabled(true);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        loginViewModel.setVerificationId(verificationId);
                        btnLogin.setEnabled(true);
                        showOTPDialog();
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOTPDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Login Code");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = input.getText().toString();
            if (!TextUtils.isEmpty(code)) {
                String savedVerificationId = loginViewModel.getVerificationId();
                if (savedVerificationId != null) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(savedVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                } else {
                    Toast.makeText(LoginActivity.this, "Verification ID not found.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                        if (isNewUser) {
                            if (mAuth.getCurrentUser() != null) {
                                mAuth.getCurrentUser().delete();
                            }
                            mAuth.signOut();

                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Account Not Found")
                                    .setMessage("There is no account associated with this phone number. Please register first.")
                                    .setPositiveButton("Go to Register", (dialog, which) -> {
                                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                                        finish();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        } else {
                            handleSuccessfulLogin();
                        }
                    } else {
                        showErrorMsg(task.getException());
                        btnLogin.setEnabled(true);
                    }
                });
    }

    private void handleSuccessfulLogin() {
        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, EventsCatalogActivity.class);
        startActivity(intent);
        finish();
    }

    private void showErrorMsg(Exception e) {
        String errorMsg = "Unknown error occurred.";

        if (e != null) {
            errorMsg = e.getMessage();
        }

        Toast.makeText(this, "Login Failed: " + errorMsg, Toast.LENGTH_LONG).show();
    }
}