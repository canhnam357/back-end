package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_ChangeOrderStatus;
import org.springframework.http.ResponseEntity;

public interface OrderStatusHistoryService {
    ResponseEntity<GenericResponse> changeOrderStatus(String userId, Req_ChangeOrderStatus reqChangeOrderStatus);

    ResponseEntity<GenericResponse> getAll(int index, int size);

    ResponseEntity<GenericResponse> getByOrder(String orderId, int index, int size);
}
