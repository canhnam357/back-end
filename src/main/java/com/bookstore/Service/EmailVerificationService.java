package com.bookstore.Service;

import com.bookstore.Entity.EmailVerification;

import java.util.Optional;

public interface EmailVerificationService {
    void sendOtp(String email);
    Optional<EmailVerification> findByEmail(String email);
//    void deleteExpiredOtp();
//    boolean verifyOtp(String email, String otp);

}
