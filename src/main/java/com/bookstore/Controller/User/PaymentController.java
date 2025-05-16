package com.bookstore.Controller.User;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Order;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.OrderService;
import com.bookstore.Service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    private final OrderService orderService;

    @Value("${user-url}")
    private String userUrl;

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/api/create-order")
    @ResponseBody
    public ResponseEntity<GenericResponse> createOrderCARD(@RequestHeader("Authorization") String authorizationHeader, HttpServletRequest request, @RequestBody Req_Create_Order order) {
        ResponseEntity<GenericResponse> res = orderService.createOrder(order, authorizationHeader);

        System.err.println(authorizationHeader);

        if (!Objects.requireNonNull(res.getBody()).getSuccess()) {
            return res;
        }

        System.err.println("SEND CREATED ORDER NOTIFICATION");

        emailVerificationService.createdOrderNotification(res.getBody().getResult().toString());


        if (order.getPaymentMethod().equals("COD")) {
            return res;
        }

        System.err.println("CARD , IP : ");

        // Tạo URL cho VNPay
        String clientIp = request.getRemoteAddr();
        System.err.println(clientIp);
        String vnPayUrl = vnPayService.createOrder(clientIp, res.getBody().getResult().toString());

        System.err.println(vnPayUrl);

        res.getBody().setResult(vnPayUrl);

        return res;
    }

    @GetMapping("/payment-return")
    public ResponseEntity<Void> paymentCompleted(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);

        System.err.println("PAYMENT STATUS " + paymentStatus);

        System.err.println("REQUEST " + request.toString());

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        if (totalPrice != null) {
            BigDecimal amount = new BigDecimal(totalPrice);
            amount = amount.divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);
            totalPrice = amount.toString();
        }

        System.err.println(paymentTime);

        // Tạo URL redirect về frontend với query parameters
        String redirectUrl = userUrl + "/payment-return" +
                "?paymentStatus=" + paymentStatus +
                "&orderId=" + (orderInfo != null ? orderInfo : "") +
                "&paymentTime=" + (paymentTime != null ? paymentTime : "") +
                "&transactionId=" + (transactionId != null ? transactionId : "") +
                "&totalPrice=" + (totalPrice != null ? totalPrice : "");

        // Redirect về frontend
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }
}
