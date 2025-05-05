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
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Utils.VNPayConfig;
import com.bookstore.Service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayServiceImpl implements VNPayService {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Value("${vnpay.apiUrl}")
    private String vnp_ApiUrl;

    @Autowired
    private VNPayConfig vnPayConfig;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RefundAttemptRepository refundAttemptRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createOrder(String ipAddress, String orderId){
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String orderType = "order";

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

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());

        Orders order = ordersRepository.findById(orderId).get();
        order.setExpireDatePayment(cld.getTime());
        ordersRepository.save(order);

        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        System.err.println(hashData.toString());
        System.err.println(vnp_HashSecret);
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    @Transactional
    public boolean refundOrder(String orderId, String txnRef, String transactionNo, String transactionDate, String createdBy, String amount, String ipAddress) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "refund";
            String vnp_RequestId = UUID.randomUUID().toString();
            String vnp_TransactionType = "02";

            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_RequestId", vnp_RequestId);
            vnpParams.put("vnp_Version", vnp_Version);
            vnpParams.put("vnp_Command", vnp_Command);
            vnpParams.put("vnp_TmnCode", vnp_TmnCode);
            vnpParams.put("vnp_TransactionType", vnp_TransactionType);
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_Amount", amount);
            vnpParams.put("vnp_OrderInfo", "Refund for order " + orderId);
            vnpParams.put("vnp_TransactionNo", transactionNo);
            vnpParams.put("vnp_TransactionDate", transactionDate);
            vnpParams.put("vnp_CreateBy", createdBy);
            vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            vnpParams.put("vnp_IpAddr", ipAddress);

            String vnp_data = vnpParams.get("vnp_RequestId") + "|" +
                    vnpParams.get("vnp_Version") + "|" +
                    vnpParams.get("vnp_Command") + "|" +
                    vnpParams.get("vnp_TmnCode") + "|" +
                    vnpParams.get("vnp_TransactionType") + "|" +
                    vnpParams.get("vnp_TxnRef") + "|" +
                    vnpParams.get("vnp_Amount") + "|" +
                    vnpParams.get("vnp_TransactionNo") + "|" +
                    vnpParams.get("vnp_TransactionDate") + "|" +
                    vnpParams.get("vnp_CreateBy") + "|" +
                    vnpParams.get("vnp_CreateDate") + "|" +
                    vnpParams.get("vnp_IpAddr") + "|" +
                    vnpParams.get("vnp_OrderInfo");

            String vnp_SecureHash = vnPayConfig.hmacSHA512(vnp_HashSecret, vnp_data);
            vnpParams.put("vnp_SecureHash", vnp_SecureHash);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(vnpParams, headers);

            Map<String, String> response = restTemplate.postForObject(vnp_ApiUrl, request, Map.class);

            if (response == null || !response.containsKey("vnp_ResponseCode")) {
                throw new IllegalStateException("Invalid response from VNPay");
            }

            String ResponseId = response.getOrDefault("vnp_ResponseId", "");
            String Command = response.getOrDefault("vnp_Command", "");
            String ResponseCode = response.getOrDefault("vnp_ResponseCode", "");
            String Message = response.getOrDefault("vnp_Message", "");
            String TmnCode = response.getOrDefault("vnp_TmnCode", "");
            String TxnRef = response.getOrDefault("vnp_TxnRef", "");
            String Amount = response.getOrDefault("vnp_Amount", "");
            String BankCode = response.getOrDefault("vnp_BankCode", "");
            String PayDate = response.getOrDefault("vnp_PayDate", "");
            String TransactionNo = response.getOrDefault("vnp_TransactionNo", "");
            String TransactionType = response.getOrDefault("vnp_TransactionType", "");
            String TransactionStatus = response.getOrDefault("vnp_TransactionStatus", "");
            String OrderInfo = response.getOrDefault("vnp_OrderInfo", "");
            String SecureHash = response.get("vnp_SecureHash");

            String data = ResponseId + "|" + Command + "|" + ResponseCode + "|" + Message + "|" + TmnCode + "|" + TxnRef +
                    "|" + Amount + "|" + BankCode + "|" + PayDate + "|" + TransactionNo + "|" + TransactionType + "|" +
                    TransactionStatus + "|" + OrderInfo;

            System.err.println("DATA Response from VNPAY Refund: " + data);

            String computedHash = vnPayConfig.hmacSHA512(vnp_HashSecret, data);
            if (!computedHash.equals(SecureHash)) {
                throw new IllegalStateException("Invalid response hash from VNPay");
            }
            Orders orders = ordersRepository.findById(orderId).get();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            orders.setRefundAt(formatter.parse(PayDate));
            ordersRepository.save(orders);
            ordersRepository.flush(); // Flush để đảm bảo dữ liệu được cập nhật ngay lập tức
            emailVerificationService.refundOrderNotification(orders); // Truyền đối tượng orders đã cập nhật
            return "00".equals(ResponseCode);
        } catch (Exception ex) {
            throw new IllegalStateException("Refund failed: " + ex.getMessage(), ex);
        }
    }

    public int orderReturn(HttpServletRequest request){
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
                System.err.println(fieldName + " " + fieldValue);
            }
        }

        String orderId = request.getParameter("vnp_OrderInfo");
        Orders order = ordersRepository.findById(orderId).get();

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
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
        Date now = new Date();
        List<Orders> expiredOrders = ordersRepository.findByPaymentMethodAndPaymentStatusAndExpireDatePaymentBefore(
                PaymentMethod.CARD, PaymentStatus.PENDING, now
        );

        for (Orders order : expiredOrders) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            for (OrderItem orderItem : order.getOrderDetails()) {
                Book book = bookRepository.findById(orderItem.getBookId()).get();
                book.setInStock(book.getInStock() + orderItem.getQuantity());
                bookRepository.save(book);
            }
        }

        ordersRepository.saveAll(expiredOrders);
    }

    @Scheduled(fixedRate = 15 * 60 * 1000)
    @Transactional
    public void refundPendingOrders() {
        System.err.println("START REFUND PROCESSING!!!");
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, -1);
        Date oneHourAgo = calendar.getTime();

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
            attempt.setOrderId(order.getOrderId());
            attempt.setTransactionNo(order.getTransactionNo());
            attempt.setTransactionDate(order.getTransactionDate());
            attempt.setAmount(order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue());
            attempt.setCreatedBy("system");
            attempt.setIpAddress("127.0.0.1"); // Giả định IP, có thể thay bằng logic lấy IP thực tế
            attempt.setStatus(RefundStatus.PENDING_REFUND);
            attempt.setAttemptTime(new Date());
            attempt.setAttemptCount(refundAttemptRepository.countByOrderId(order.getOrderId()) + 1);

            order.setRefundStatus(RefundStatus.PENDING_REFUND);
            order.setLastCallRefund(new Date());
            ordersRepository.save(order);

            try {
                boolean refundSuccess = refundOrder(
                        order.getOrderId(),
                        order.getTxnRef(),
                        order.getTransactionNo(),
                        order.getTransactionDate(),
                        "system",
                        String.valueOf(attempt.getAmount()),
                        attempt.getIpAddress()
                );

                attempt.setStatus(refundSuccess ? RefundStatus.REFUNDED : RefundStatus.FAILED_REFUND);
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