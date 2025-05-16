package com.bookstore.Controller.Admin;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.OrderStatusHistoryService;
import com.bookstore.Service.RefundAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/refund-status")
public class Admin_RefundController {
    @Autowired
    private RefundAttemptService refundAttemptService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "") String orderId, @RequestParam(defaultValue = "1") int index, @RequestParam(defaultValue = "10") int size) {
        System.err.println(orderId);
        return refundAttemptService.getAll(orderId, index, size);
    }
}
