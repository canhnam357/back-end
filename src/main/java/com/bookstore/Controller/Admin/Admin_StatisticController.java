package com.bookstore.Controller.Admin;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.OrderService;
import com.bookstore.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class Admin_StatisticController {

    private final OrderService orderService;

    private final UserService userService;

    @GetMapping("/monthly-revenue")
    ResponseEntity<GenericResponse> getMonthlyRevenue(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        return orderService.getMonthlyRevenue(year);
    }

    @GetMapping("/count-verified-user")
    ResponseEntity<GenericResponse> countVerifiedUsersByMonth(@RequestParam(required = false) Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        return userService.countVerifiedUsersByMonth(year);
    }

    @GetMapping("/order-by-status")
    ResponseEntity<GenericResponse> getOrderCountByStatus() {
        return orderService.getOrderCountByStatus();
    }

}
