package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.Constant.RefundStatus;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.OrderItem;
import com.bookstore.Entity.Orders;
import com.bookstore.Entity.RefundAttempt;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.OrdersRepository;
import com.bookstore.Repository.RefundAttemptRepository;
import com.bookstore.Service.RefundAttemptService;
import com.bookstore.Utils.VNPayConfig;
import com.bookstore.Service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    private final VNPayConfig vnPayConfig;
    private final OrdersRepository ordersRepository;
    private final BookRepository bookRepository;
    private final RefundAttemptRepository refundAttemptRepository;
    private final RefundAttemptService refundAttemptService;


    public String createOrder(String ipAddress, String orderId){
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String orderType = "order";

        assert (ordersRepository.findById(orderId).isPresent());
        BigDecimal amount = ordersRepository.findById(orderId).get().getTotalPrice().multiply(BigDecimal.valueOf(100));

        long _amount = amount.longValue();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(_amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderId);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", ipAddress);

        System.err.println("IP USER SEND ORDER : " + ipAddress);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String vnp_CreateDate = now.format(formatter);
        String vnp_ExpireDate = now.plusMinutes(15).format(formatter);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);


        Orders order = ordersRepository.findById(orderId).get();
        order.setExpireDatePayment(now.plusMinutes(15));
        ordersRepository.save(order);


        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        System.err.println("HASH DATA " + hashData);
        System.err.println("VNP_HASH_SECRET " + vnp_HashSecret);
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    public int orderReturn(HttpServletRequest request){
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName;
            String fieldValue;
            fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII);
            fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
                System.err.println(fieldName + " " + fieldValue);
            }
        }

        String orderId = request.getParameter("vnp_OrderInfo");
        assert (ordersRepository.findById(orderId).isPresent());
        Orders order = ordersRepository.findById(orderId).get();

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = vnPayConfig.hashAllFields(fields);
        System.err.println(signValue);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                order.setPaymentStatus(PaymentStatus.SUCCESS);
                order.setTransactionNo(request.getParameter("vnp_TransactionNo"));
                order.setTransactionDate(request.getParameter("vnp_PayDate"));
                order.setTxnRef(request.getParameter("vnp_TxnRef"));
                ordersRepository.save(order);
                return 1;
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setOrderStatus(OrderStatus.CANCELLED);
                for (OrderItem orderItem : order.getOrderDetails()) {
                    assert (bookRepository.findById(orderItem.getBookId()).isPresent());
                    Book book = bookRepository.findById(orderItem.getBookId()).get();
                    book.setInStock(book.getInStock() + orderItem.getQuantity());
                    bookRepository.save(book);
                }
                ordersRepository.save(order);
                return 0;
            }
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            for (OrderItem orderItem : order.getOrderDetails()) {
                assert (bookRepository.findById(orderItem.getBookId()).isPresent());
                Book book = bookRepository.findById(orderItem.getBookId()).get();
                book.setInStock(book.getInStock() + orderItem.getQuantity());
                bookRepository.save(book);
            }
            ordersRepository.save(order);
            return -1;
        }
    }

    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Transactional
    public void cancelExpiredOrders() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<Orders> expiredOrders = ordersRepository.findByPaymentMethodAndPaymentStatusAndExpireDatePaymentBefore(
                PaymentMethod.CARD, PaymentStatus.PENDING, now
        );

        for (Orders order : expiredOrders) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            for (OrderItem orderItem : order.getOrderDetails()) {
                assert (bookRepository.findById(orderItem.getBookId()).isPresent());
                Book book = bookRepository.findById(orderItem.getBookId()).get();
                book.setInStock(book.getInStock() + orderItem.getQuantity());
                bookRepository.save(book);
            }
        }

        ordersRepository.saveAll(expiredOrders);
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void refundPendingOrders() {
        System.err.println("START REFUND PROCESSING!!!");
        ZonedDateTime oneHourAgo = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusHours(1);

        List<Orders> ordersToRefund = ordersRepository.findOrdersForRefund(
                OrderStatus.CANCELLED,
                OrderStatus.RETURNED,
                OrderStatus.REJECTED,
                PaymentMethod.CARD,
                PaymentStatus.SUCCESS,
                RefundStatus.PENDING_REFUND,
                RefundStatus.REFUNDED,
                oneHourAgo
        );

        for (Orders order : ordersToRefund) {
            RefundAttempt attempt = new RefundAttempt();
            attempt.setTransactionNo(order.getTransactionNo());
            attempt.setTransactionDate(order.getTransactionDate());
            attempt.setAmount(order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue());
            attempt.setCreatedBy("system");
            attempt.setIpAddress("127.0.0.1");
            attempt.setStatus(RefundStatus.PENDING_REFUND);
            attempt.setAttemptTime(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            attempt.setAttemptCount(refundAttemptRepository.countByOrderOrderId(order.getOrderId()) + 1);
            attempt.setOrder(order);
            order.setRefundStatus(RefundStatus.PENDING_REFUND);
            order.setLastCallRefund(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            ordersRepository.save(order);

            try {
                boolean refundSuccess = refundAttemptService.refundOrder(
                        order.getOrderId(),
                        order.getTxnRef(),
                        order.getTransactionNo(),
                        order.getTransactionDate(),
                        "system",
                        String.valueOf(attempt.getAmount()),
                        attempt.getIpAddress()
                );

                attempt.setStatus(refundSuccess ? RefundStatus.REFUNDED : RefundStatus.FAILED_REFUND);
                if (refundSuccess) {
                    System.err.println("REFUNDED order : " + order.getOrderId() + " successfully!");
                }
                else {
                    System.err.println("FAILED to refund order : " + order.getOrderId());
                }
                attempt.setErrorMessage(refundSuccess ? null : "VNPay refund failed");
                order.setRefundStatus(attempt.getStatus());
            } catch (Exception e) {
                attempt.setStatus(RefundStatus.FAILED_REFUND);
                attempt.setErrorMessage("Refund failed: " + e.getMessage());
                order.setRefundStatus(RefundStatus.FAILED_REFUND);
            }

            order.setRefundTimesRemain(order.getRefundTimesRemain() - 1);
            refundAttemptRepository.save(attempt);
            ordersRepository.save(order);
        }
    }
}