package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface RefundAttemptService {
    ResponseEntity<GenericResponse> getAll(String orderId, int index, int size);
    boolean refundOrder(String orderId, String txnRef, String transactionNo, String transactionDate, String createdBy, String amount, String ipAddress);
}
