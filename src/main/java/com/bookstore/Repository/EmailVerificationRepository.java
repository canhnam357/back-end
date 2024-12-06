package com.bookstore.Repository;

import com.bookstore.Entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    List<EmailVerification> findByExpirationTimeBefore(LocalDateTime expirationTime);
    Optional<EmailVerification> findByEmail(String email);

    Optional<EmailVerification> findByOtp(String otp);
}
