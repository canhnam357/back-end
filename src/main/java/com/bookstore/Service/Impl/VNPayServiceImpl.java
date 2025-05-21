package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.Constant.RefundStatus;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Res_PaymentInfo;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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
    @Value("${vnpay.apiUrl}")
    private String vnp_ApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    @Transactional(rollbackFor = Exception.class)
    public String createOrder(String ipAddress, String orderId){
        log.info("Bắt đầu tạo thanh toán VNPay!");
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

        log.info("IP USER SEND ORDER : " + ipAddress);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String vnp_CreateDate = now.format(formatter);
        String vnp_ExpireDate = now.plusMinutes(15).format(formatter);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);


        Orders order = ordersRepository.findById(orderId).get();
        order.setExpireDatePayment(now.plusMinutes(15));


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
        log.info("HASH DATA " + hashData);
        log.info("VNP_HASH_SECRET " + vnp_HashSecret);
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        log.info("Tạo thanh toán VNPay thành công!");
        order.setPaymentUrl(vnPayConfig.vnp_PayUrl + "?" + queryUrl);
        ordersRepository.save(order);
        return vnPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    @Transactional(rollbackFor = Exception.class)
    public int orderReturn(HttpServletRequest request){
        Map fields = new HashMap();
        StringBuilder data = new StringBuilder();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName;
            String fieldValue;
            fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII);
            fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
                if (data.length() > 0) data.append(",");
                data.append(fieldName).append("=").append(fieldValue);
            }
        }

        log.info("Date response payment from vnpay = " + data);

        String orderId = request.getParameter("vnp_OrderInfo");
        assert (ordersRepository.findById(orderId).isPresent());
        Orders order = ordersRepository.findById(orderId).get();

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        String signValue = vnPayConfig.hashAllFields(fields);
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

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getPaymentDetail(String userId, String orderId) {
        try {
            if (ordersRepository.findById(orderId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("Đơn hàng không tồn tại!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }
            Orders order = ordersRepository.findById(orderId).get();

            if (!order.getUser().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Đơn hàng này không phải của bạn!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (order.getPaymentMethod() != PaymentMethod.CARD) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Phương thức thanh toán không phải Thẻ tín dụng!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (order.getPaymentStatus() != PaymentStatus.SUCCESS) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Trạng thái thanh toán chưa thành công!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }
            log.info("Bắt đầu refund!");
            String vnp_Version = "2.1.0";
            String vnp_Command = "querydr";
            String vnp_RequestId = UUID.randomUUID().toString();
            String txnRef = order.getTxnRef();
            String transactionNo = order.getTransactionNo();
            String transactionDate = order.getTransactionDate();
            String ipAddress = "127.0.0.1";


            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_RequestId", vnp_RequestId);
            vnpParams.put("vnp_Version", vnp_Version);
            vnpParams.put("vnp_Command", vnp_Command);
            vnpParams.put("vnp_TmnCode", vnp_TmnCode);
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", "Refund for order " + orderId);
            vnpParams.put("vnp_TransactionNo", transactionNo);
            vnpParams.put("vnp_TransactionDate", transactionDate);
            vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            vnpParams.put("vnp_IpAddr", ipAddress);

            String vnp_data = vnpParams.get("vnp_RequestId") + "|" +
                    vnpParams.get("vnp_Version") + "|" +
                    vnpParams.get("vnp_Command") + "|" +
                    vnpParams.get("vnp_TmnCode") + "|" +
                    vnpParams.get("vnp_TxnRef") + "|" +
                    vnpParams.get("vnp_TransactionDate") + "|" +
                    vnpParams.get("vnp_CreateDate") + "|" +
                    vnpParams.get("vnp_IpAddr") + "|" +
                    vnpParams.get("vnp_OrderInfo");

            log.info("vnp_data: " + vnp_data);

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
            String PromotionCode = response.getOrDefault("vnp_PromotionCode", "");
            String PromotionAmount = response.getOrDefault("vnp_PromotionAmount", "");
            String SecureHash = response.get("vnp_SecureHash");

            String data = ResponseId + "|" + Command + "|" + ResponseCode + "|" + Message + "|" + TmnCode + "|" + TxnRef +
                    "|" + Amount + "|" + BankCode + "|" + PayDate + "|" + TransactionNo + "|" + TransactionType + "|" +
                    TransactionStatus + "|" + OrderInfo + "|" + PromotionCode + "|" + PromotionAmount;

            log.info("DATA Response from VNPAY Querydr: " + data);

            if (ResponseCode.equals("94")) {
                Message = "Giao dịch đã được gửi yêu cầu hoàn tiền trước đó. Yêu cầu này VNPAY đang xử lý";
            }
            else if (ResponseCode.equals("95")) {
                Message = "Giao dịch này không thành công bên VNPAY. VNPAY từ chối xử lý yêu cầu";
            }

            if (!"00".equals(ResponseCode)) {
                throw new IllegalStateException(Message);
            }

            String computedHash = vnPayConfig.hmacSHA512(vnp_HashSecret, data);
            if (!computedHash.equals(SecureHash)) {
                throw new IllegalStateException("Invalid response hash from VNPay");
            }

            BigDecimal amount = new BigDecimal(Amount);
            amount = amount.divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy thông tin thanh toán thành công!")
                            .result(new Res_PaymentInfo(
                                    amount,
                                    BankCode,
                                    PayDate
                            ))
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Xem chi tiết thanh toán thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi : " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Scheduled(fixedRate = 60 * 1000)
    @Transactional
    public void cancelExpiredOrders() {
        log.info("START DELETE Order Payment with card expired");
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
    @Transactional
    public void refundPendingOrders() {
        log.info("START REFUND PROCESSING!!!");
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
                    log.info("REFUNDED order : " + order.getOrderId() + " successfully!");
                }
                else {
                    log.error("FAILED to refund order : " + order.getOrderId());
                }
                attempt.setErrorMessage(refundSuccess ? null : "VNPay refund failed");
                order.setRefundStatus(attempt.getStatus());
            } catch (Exception e) {
                attempt.setStatus(RefundStatus.FAILED_REFUND);
                attempt.setErrorMessage(e.getMessage());
                order.setRefundStatus(RefundStatus.FAILED_REFUND);
            }

            order.setRefundTimesRemain(order.getRefundTimesRemain() - 1);
            refundAttemptRepository.save(attempt);
            ordersRepository.save(order);
        }
    }
}