package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.OrderDTO;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    ResponseEntity<GenericResponse> createOrder(OrderDTO orderDTO, String userId);

    ResponseEntity<GenericResponse> getAll(String userId, int page, int size);
}
