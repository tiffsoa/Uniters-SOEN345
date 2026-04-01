package com.example.ticketreservationapp.ui;

import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {
    // Hold the verification ID here so it survives screen rotations
    private String verificationId;

    public String getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(String verificationId) {
        this.verificationId = verificationId;
    }
}
