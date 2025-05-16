package com.bookstore.Controller.Shipper;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'SHIPPER')")
@RequestMapping("/api/shipper/orders")
@RequiredArgsConstructor
public class Shipper_OrderController {

    private final OrderService orderService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "1") int index,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(defaultValue = "") String orderStatus) {
        return orderService.getAllForShipper(orderStatus, index, size);
    }

}
