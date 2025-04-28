package com.bookstore.Service;

import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {
    String createOrder(String urlReturn, String orderId);
    int orderReturn(HttpServletRequest request);

}