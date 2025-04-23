package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Order;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    ResponseEntity<GenericResponse> createOrder(Req_Create_Order orderDTO, String userId);

    ResponseEntity<GenericResponse> getAll(String userId, int page, int size);

}
