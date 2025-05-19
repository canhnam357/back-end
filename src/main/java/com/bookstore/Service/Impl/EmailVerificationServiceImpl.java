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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
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
            existingEmailVerification.ifPresent(emailVerificationRepository::delete);

            emailVerificationRepository.save(emailVerification);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Gửi OTP Đăng ký thất bại, lỗi : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        int OTP_LENGTH = 6;
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<EmailVerification> findByEmail(String email) {
        return emailVerificationRepository.findByEmail(email);
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
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
            existingEmailVerification.ifPresent(emailVerificationRepository::delete);

            emailVerificationRepository.save(emailVerification);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Gửi OTP đổi mật khẩu thất bại, lỗi : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> sendOTPResetPassword(String email) {
        try {

            if (email.length() > 255) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Email có độ dài tối đa 255 ký tự!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (userRepository.findByEmail(email).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tồn tại tài khoản với email này!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            String otp = generateOtp();
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
            existingEmailVerification.ifPresent(emailVerificationRepository::delete);

            emailVerificationRepository.save(emailVerification);

            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Gửi OTP Đặt lại mật khẩu thành công, vui lòng kiểm tra Email!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception e) {
            log.error("Gửi OTP quên mật khẩu thất bại, lỗi : " + e.getMessage());
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
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
            context.setVariable("totalOrderValue", order.getTotalPrice());
            context.setVariable("orderAt", order.getOrderAt());
            String mailContent = templateEngine.process("order-confirmation", context);
            helper.setText(mailContent, true);
            helper.setSubject("Order Confirmation");
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Gửi mail xác nhận đơn hàng thất bại, lỗi : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED, rollbackFor = Exception.class)
    @Async
    public void refundOrderNotification(Orders order, ZonedDateTime refundAt) {
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
            context.setVariable("estimatedRefundTime", refundAt);
            context.setVariable("refundReason", reason_for_refund);
            String mailContent = templateEngine.process("refund-notification", context);
            helper.setText(mailContent, true);
            helper.setSubject("Refund Order Notification");
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Gửi mail hoàn tiền đơn hàng thất bại, lỗi : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
