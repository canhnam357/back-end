package com.bookstore.Controller.User;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Order;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.OrderService;
import com.bookstore.Service.OrderStatusHistoryService;
import com.bookstore.Service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER', 'SHIPPER')")
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    private OrderStatusHistoryService orderStatusHistoryService;

    @PostMapping("")
    public ResponseEntity<GenericResponse> createOrderCOD(@RequestHeader("Authorization") String authorizationHeader,
                                                     @RequestBody Req_Create_Order orderDTO) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("create order " + userId);
        return orderService.createOrder(orderDTO, userId);
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "100") int size,
                                                  @RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestParam(defaultValue = "") String orderStatus) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        System.err.println("getAll order " + userId);
        return orderService.getAllOfUser(userId, orderStatus, page, size);
    }

    @PutMapping("/change-order-status")
    public ResponseEntity<GenericResponse> changeOrderStatus(@RequestHeader("Authorization") String authorizationHeader,
                                                             @RequestParam("orderId") String orderId,
                                                             @RequestParam("fromStatus") String fromStatus,
                                                             @RequestParam("toStatus") String toStatus) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderStatusHistoryService.changeOrderStatus(userId, orderId, fromStatus, toStatus);
    }

    @GetMapping("/order-detail/{orderId}")
    public ResponseEntity<GenericResponse> orderDetail(@RequestHeader("Authorization") String authorizationHeader,
                                                       @PathVariable String orderId) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return orderService.orderDetail(userId, orderId);
    }

}
