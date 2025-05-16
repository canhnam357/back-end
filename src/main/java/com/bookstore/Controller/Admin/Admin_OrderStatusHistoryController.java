package com.bookstore.Controller.Admin;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/order-status")
@RequiredArgsConstructor
public class Admin_OrderStatusHistoryController {
    private final OrderStatusHistoryService orderStatusHistoryService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "") String orderId, @RequestParam(defaultValue = "1") int index, @RequestParam(defaultValue = "10") int size) {
        return orderStatusHistoryService.getByOrder(orderId, index, size);
    }
}
