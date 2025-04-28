package com.bookstore.Controller.Employee;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@RequestMapping("/api/employee/orders")
public class Employee_OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "1") int index,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "") String orderStatus) {
        return orderService.getAll(orderStatus, index, size);
    }

}
