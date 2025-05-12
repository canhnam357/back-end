package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.EmailVerification;
import com.bookstore.Entity.Orders;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface EmailVerificationService {
    void sendOtp(String email);
    Optional<EmailVerification> findByEmail(String email);

    void sendOTPChangePassword(String userId);

    ResponseEntity<GenericResponse> sendOTPResetPassword(String email);

    void createdOrderNotification(String orderId);

    void refundOrderNotification(Orders orders);
}
