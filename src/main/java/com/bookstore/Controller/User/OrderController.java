package com.bookstore.Controller.User;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_ChangeOrderStatus;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.OrderService;
import com.bookstore.Service.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER', 'SHIPPER')")
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrderStatusHistoryService orderStatusHistoryService;
    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "1") int index,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestParam(defaultValue = "") String orderStatus) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("getAll order " + userId);
        return orderService.getAllOfUser(userId, orderStatus, index, size);
    }

    @PutMapping("/change-order-status")
    public ResponseEntity<GenericResponse> changeOrderStatus(@RequestHeader("Authorization") String authorizationHeader,
                                                             @RequestBody Req_ChangeOrderStatus reqChangeOrderStatus) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderStatusHistoryService.changeOrderStatus(userId, reqChangeOrderStatus);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GenericResponse> orderDetail(@RequestHeader("Authorization") String authorizationHeader,
                                                       @PathVariable String orderId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderService.orderDetail(userId, orderId);
    }

}
