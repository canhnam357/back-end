package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface OrderStatusHistoryService {
    ResponseEntity<GenericResponse> changeOrderStatus(String userId, String orderId, String fromStatus, String toStatus);
}
