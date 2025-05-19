package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Order;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    ResponseEntity<GenericResponse> createOrder(Req_Create_Order orderDTO, String authorizationHeader);

    ResponseEntity<GenericResponse> getAllOfUser(String userId, String orderStatus, int page, int size);

    ResponseEntity<GenericResponse> orderDetail(String userId, String orderId);

    ResponseEntity<GenericResponse> getAll(String orderStatus, int index, int size);

    ResponseEntity<GenericResponse> getAllForShipper(String orderStatus, int index, int size);

    ResponseEntity<GenericResponse> getMonthlyRevenue(int year);

    ResponseEntity<GenericResponse> getOrderCountByStatus();
}
