package com.bookstore.Controller.User;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.OrderDTO;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER')")
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("")
    public ResponseEntity<GenericResponse> createOrder(@RequestHeader("Authorization") String authorizationHeader,
                                                     @RequestBody OrderDTO orderDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("create order " + userId);
        return orderService.createOrder(orderDTO, userId);
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "100") int size,
                                                  @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("getAll order " + userId);
        return orderService.getAll(userId, page, size);
    }
}
