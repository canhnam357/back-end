package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.EmailVerification;
import com.bookstore.Entity.Orders;
import com.bookstore.Repository.EmailVerificationRepository;
import com.bookstore.Repository.OrdersRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.EmailVerificationService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private OrdersRepository ordersRepository;

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
                    .message("Send OTP ResetPassword failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional
    @Async
    public void createdOrderNotification(String orderId) {
        try {

            if (ordersRepository.findById(orderId).isEmpty()) {
                return;
            }

            Orders order = ordersRepository.findById(orderId).get();

            String email = order.getUser().getEmail();

            if (userRepository.findByEmail(email).isEmpty()) {
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("address", order.getAddress());
            context.setVariable("phone", order.getPhoneNumber());
            context.setVariable("paymentMethod", order.getPaymentMethod().name());
            context.setVariable("orderDetails", order.getOrderDetails());
            System.err.println("No OrderItems : " + order.getOrderDetails().size());
            context.setVariable("totalOrderValue", order.getTotalPrice());
            String mailContent = templateEngine.process("order-confirmation", context);
            helper.setText(mailContent, true);
            helper.setSubject("Order Confirmation");
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    @Async
    public void refundOrderNotification(Orders order) {
        try {
            String email = order.getUser().getEmail();

            if (userRepository.findByEmail(email).isEmpty()) {
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);

            // Load Thymeleaf template
            Context context = new Context();
            context.setVariable("orderId", order.getOrderId());
            context.setVariable("orderAt", order.getOrderAt());
            String reason_for_refund = "";
            if (order.getOrderStatus().equals(OrderStatus.CANCELLED)) {
                reason_for_refund = "Bạn huỷ đơn!";
            }
            else if (order.getOrderStatus().equals(OrderStatus.REJECTED)) {
                reason_for_refund = "Nhân viên từ chối đơn hàng!";
            }
            else if (order.getOrderStatus().equals(OrderStatus.RETURNED)) {
                reason_for_refund = "Giao hàng thất bại!";
            }
            System.err.println(order.getRefundAt());
            context.setVariable("estimatedRefundTime", order.getRefundAt());
            context.setVariable("refundReason", reason_for_refund);
            context.setVariable("orderDetails", order.getOrderDetails());
            System.err.println("No OrderItems : " + order.getOrderDetails().size());
            context.setVariable("totalOrderValue", order.getTotalPrice());
            String mailContent = templateEngine.process("refund-notification", context);
            helper.setText(mailContent, true);
            helper.setSubject("Refund Order Notification");
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
