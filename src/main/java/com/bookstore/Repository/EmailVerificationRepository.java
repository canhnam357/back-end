package com.bookstore.Repository;

import com.bookstore.Entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {
    Optional<EmailVerification> findByEmail(String email);

    Optional<EmailVerification> findByOtpAndEmail(String otp, String email);
}
