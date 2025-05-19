package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface VNPayService {
    String createOrder(String urlReturn, String orderId);
    int orderReturn(HttpServletRequest request);

    ResponseEntity<GenericResponse> getPaymentDetail(String userId, String orderId);
}