package com.bookstore.Service.Impl;

import com.bookstore.BookStoreApplication;
import com.bookstore.Entity.EmailVerification;
import com.bookstore.Repository.EmailVerificationRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.EmailVerificationService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
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
    private UserRepository userRepository;
    @Autowired
    TemplateEngine templateEngine;
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

//    @Override
//    public boolean verifyOtp(String email, String otp) {
//        Optional<EmailVerification> emailVerification = emailVerificationRepository.findByEmail(email);
//        Optional<User> optionalUser = userRepository.findByEmail(email);
//        if (optionalUser.isPresent() && emailVerification.isPresent() && emailVerification.get().getOtp().equals(otp)) {
//            User user = optionalUser.get();
//            user.setVerified(true);
//            userRepository.save(user);
//            return true;
//        }
//        return false;
//    }
//
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
//    @Override
//    public void deleteExpiredOtp() {
//        LocalDateTime now = LocalDateTime.now();
//        List<EmailVerification> expiredOtpList = emailVerificationRepository.findByExpirationTimeBefore(now);
//        emailVerificationRepository.deleteAll(expiredOtpList);
//    }
//    @Scheduled(fixedDelay = 30000) // 5 minutes
//    public void cleanupExpiredOtp() {
//        deleteExpiredOtp();
//    }
}
