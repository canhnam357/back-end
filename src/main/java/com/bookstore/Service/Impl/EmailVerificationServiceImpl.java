package com.bookstore.Service.Impl;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.EmailVerification;
import com.bookstore.Repository.EmailVerificationRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.EmailVerificationService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@EnableScheduling
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final int OTP_LENGTH = 6;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void sendOtp(String email) {
        String otp = generateOtp();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("otpCode", otp);
            context.setVariable("verifyEmail", email);
            String mailContent = templateEngine.process("send-otp", context);

            helper.setText(mailContent, true);
            helper.setSubject("The verification token for BookShop");
            mailSender.send(message);

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
            EmailVerification emailVerification = new EmailVerification();
            emailVerification.setEmail(email);
            emailVerification.setOtp(otp);
            emailVerification.setExpirationTime(expirationTime);

            Optional<EmailVerification> existingEmailVerification = findByEmail(email);
            if (existingEmailVerification.isPresent()) {
                emailVerificationRepository.delete(existingEmailVerification.get());
            }

            emailVerificationRepository.save(emailVerification);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    @Override
    public Optional<EmailVerification> findByEmail(String email) {
        return emailVerificationRepository.findByEmail(email);
    }

    @Override
    public void sendOTPChangePassword(String userId) {
        String otp = generateOtp();
        try {

            if (userRepository.findById(userId).isEmpty()) {
                return;
            }

            String email = userRepository.findById(userId).get().getEmail();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("otpCode", otp);
            String mailContent = templateEngine.process("otp-reset-password", context);

            helper.setText(mailContent, true);
            helper.setSubject("The OTP Reset password for BookShop account");
            mailSender.send(message);

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
            EmailVerification emailVerification = new EmailVerification();
            emailVerification.setEmail(email);
            emailVerification.setOtp(otp);
            emailVerification.setExpirationTime(expirationTime);

            Optional<EmailVerification> existingEmailVerification = findByEmail(email);
            if (existingEmailVerification.isPresent()) {
                emailVerificationRepository.delete(existingEmailVerification.get());
            }

            emailVerificationRepository.save(emailVerification);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<GenericResponse> sendOTPResetPassword(String email) {
        String otp = generateOtp();
        try {

            if (userRepository.findByEmail(email).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found email!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("otpCode", otp);
            String mailContent = templateEngine.process("otp-reset-password", context);

            helper.setText(mailContent, true);
            helper.setSubject("The OTP Reset password for BookShop account");
            mailSender.send(message);

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
            EmailVerification emailVerification = new EmailVerification();
            emailVerification.setEmail(email);
            emailVerification.setOtp(otp);
            emailVerification.setExpirationTime(expirationTime);

            Optional<EmailVerification> existingEmailVerification = findByEmail(email);
            if (existingEmailVerification.isPresent()) {
                emailVerificationRepository.delete(existingEmailVerification.get());
            }

            emailVerificationRepository.save(emailVerification);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Send OTP ResetPassword success!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Sen OTP ResetPassword failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
