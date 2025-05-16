package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Res_Refund;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Orders;
import com.bookstore.Entity.RefundAttempt;
import com.bookstore.Repository.OrdersRepository;
import com.bookstore.Repository.RefundAttemptRepository;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.RefundAttemptService;
import com.bookstore.Utils.VNPayConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RefundAttemptServiceImpl implements RefundAttemptService {
    private final RefundAttemptRepository refundAttemptRepository;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime refundAt;

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    private final VNPayConfig vnPayConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vnpay.apiUrl}")
    private String vnp_ApiUrl;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    private final OrdersRepository ordersRepository;

    private final EmailVerificationService emailVerificationService;

    @Override
    public ResponseEntity<GenericResponse> getAll(String orderId, int index, int size) {
        try {
            Page<RefundAttempt> refundAttempts;
            if (!orderId.isEmpty()) {
                refundAttempts = refundAttemptRepository.findByOrderOrderIdOrderByAttemptTimeDesc(orderId, PageRequest.of(index - 1, size));
            }
            else {
                refundAttempts = refundAttemptRepository.findAllByOrderByAttemptTimeDesc(PageRequest.of(index - 1, size));
            }
            List<Admin_Res_Refund> res = new ArrayList<>();
            for (RefundAttempt refundAttempt : refundAttempts) {
                Admin_Res_Refund temp = new Admin_Res_Refund();
                temp.convert(refundAttempt);
                res.add(temp);
            }
            Page<Admin_Res_Refund> dtoPage = new PageImpl<>(res, refundAttempts.getPageable(), refundAttempts.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Retrieved all refund status history successfully!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(false)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Failed to retrieve refund status history, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Transactional
    public boolean refundOrder(String orderId, String txnRef, String transactionNo, String transactionDate, String createdBy, String amount, String ipAddress) {
        String Message;
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

            System.err.println("vnp_data: " + vnp_data);

            String vnp_SecureHash = vnPayConfig.hmacSHA512(vnp_HashSecret, vnp_data);
            vnpParams.put("vnp_SecureHash", vnp_SecureHash);

            System.err.println("vnp_SecureHash = " + vnp_SecureHash);
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
            Message = response.getOrDefault("vnp_Message", "");
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
            System.err.println("VNPay returned PayDate = '" + PayDate + "'");
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

                LocalDateTime localPayDate = LocalDateTime.parse(PayDate, formatter);

                refundAt = localPayDate.atZone(ZoneId.of("Asia/Ho_Chi_Minh"));
            } catch (Exception ex) {
                throw new IllegalStateException("Parse PayDate failed: " + PayDate);
            }

            Orders orders = ordersRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

            try {
                ordersRepository.flush();
                System.err.println(">> flush() thành công");
            } catch (Exception e) {
                System.err.println("!!! Error on flush(): " + e.getMessage());
                throw e;
            }

            // 3) Gửi email không được throw exception phá luồng
            try {
                emailVerificationService.refundOrderNotification(orders, refundAt);
            } catch (Exception e) {
                System.err.println("Warning: refund notification failed: " + e.getMessage());
            }

            if ("00".equals(ResponseCode)) {
                orders.setRefundAt(refundAt);
                ordersRepository.save(orders);
            }

            return "00".equals(ResponseCode);
        } catch (Exception ex) {
            // ex.getMessage() giờ đã là chi tiết lỗi parse hoặc order not found,
            // nếu ex do VNPay thì Message = vnp_Message ban đầu
            throw new IllegalStateException("Refund failed: " + ex.getMessage(), ex);
        }
    }
}
