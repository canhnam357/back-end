package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.*;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.OrderStatusHistoryRepository;
import com.bookstore.Repository.OrdersRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.OrderStatusHistoryService;
import com.bookstore.Utils.CheckCanChangeOrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;

@Service
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private BookRepository bookRepository;


    private boolean EMPLOYEEPermission(String roleName) {
        return roleName.equals("ADMIN") || roleName.equals("EMPLOYEE");
    }

    private boolean SHIPPERPermission(String roleName) {
        return roleName.equals("ADMIN") || roleName.equals("SHIPPER");
    }

    @Override
    @Transactional
    public ResponseEntity<GenericResponse> changeOrderStatus(String userId, String orderId, String fromStatus, String toStatus) {
        try {
            if (ordersRepository.findById(orderId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Not found order!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Orders order = ordersRepository.findById(orderId).get();
            if (fromStatus == null || toStatus == null || !Arrays.stream(OrderStatus.values()).anyMatch(e -> e.name().equals(fromStatus)) || !Arrays.stream(OrderStatus.values()).anyMatch(e -> e.name().equals(toStatus))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("fromStatus or toStatus not in OrderStatus!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (!CheckCanChangeOrderStatus.check(fromStatus, toStatus)) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Can't change fromStatus to toStatus!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            // User CANCELLED Order
            if (toStatus.equals(OrderStatus.CANCELLED.name()) && (!order.getUser().getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Users can't cancel orders that are not theirs!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            if (toStatus.equals(OrderStatus.CANCELLED.name()) && order.getPaymentMethod().equals(PaymentMethod.CARD) && order.getPaymentStatus().equals(PaymentStatus.PENDING)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Can't CANCELLED order while PaymentStatus is PENDING, wait 30 minutes and try again!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            User user = userRepository.findById(userId).get();

            if (order.getPaymentMethod().equals(PaymentMethod.CARD) && order.getPaymentStatus().equals(PaymentStatus.PENDING) && toStatus.equals(OrderStatus.REJECTED.name())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Can't REJECTED order while PaymentStatus is PENDING!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            if (order.getPaymentMethod().equals(PaymentMethod.CARD) && order.getPaymentStatus().equals(PaymentStatus.PENDING) && toStatus.equals(OrderStatus.IN_PREPARATION.name())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Can't IN_PREPARATION order while PaymentStatus is PENDING!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            // PENDING -> REJECTED
            if (!EMPLOYEEPermission(user.getRole().getName()) && toStatus.equals(OrderStatus.REJECTED.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Must have EMPLOYEE Permission to change from PENDING -> REJECTED!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // PENDING -> IN_PREPARATION
            if (!EMPLOYEEPermission(user.getRole().getName()) && toStatus.equals(OrderStatus.IN_PREPARATION.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Must have EMPLOYEE Permission to change from PENDING -> IN_PREPARATION!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // IN_PREPARATION -> READY_TO_SHIP
            if (!EMPLOYEEPermission(user.getRole().getName()) && toStatus.equals(OrderStatus.READY_TO_SHIP.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Must have EMPLOYEE Permission to change from IN_PREPARATION -> READY_TO_SHIP!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // FAILED_DELIVERY -> RETURNED
            if (!EMPLOYEEPermission(user.getRole().getName()) && toStatus.equals(OrderStatus.RETURNED.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Must have EMPLOYEE Permission to change from FAILED_DELIVERY -> RETURNED!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // READY_TO_SHIP -> DELIVERING
            if (!SHIPPERPermission(user.getRole().getName()) && toStatus.equals(OrderStatus.DELIVERING.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Must have SHIPPER Permission to change from READY_TO_SHIP -> DELIVERING!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // DELIVERING -> DELIVERED
            if (!SHIPPERPermission(user.getRole().getName()) && toStatus.equals(OrderStatus.DELIVERED.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Must have SHIPPER Permission to change from DELIVERING -> DELIVERED!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // DELIVERING -> FAILED_DELIVERY
            if (!SHIPPERPermission(user.getRole().getName()) && toStatus.equals(OrderStatus.FAILED_DELIVERY.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Must have SHIPPER Permission to change from DELIVERING -> FAILED_DELIVERY!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            order.setOrderStatus(OrderStatus.valueOf(toStatus));
            order = ordersRepository.save(order);
            if (toStatus.equals("REJECTED") || toStatus.equals("CANCELLED") || toStatus.equals("RETURNED")) {
                for (OrderItem orderItem : order.getOrderDetails()) {
                    Book book = bookRepository.findById(orderItem.getBookId())
                            .orElseThrow(() -> new RuntimeException("Book not found with ID: " + orderItem.getBookId()));
                    book.setInStock(book.getInStock() + orderItem.getQuantity());
                    bookRepository.save(book);
                }

            }
            OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
            orderStatusHistory.setOrder(order);
            orderStatusHistory.setFromStatus(OrderStatus.valueOf(fromStatus));
            orderStatusHistory.setToStatus(OrderStatus.valueOf(toStatus));
            orderStatusHistory.setChangedBy(user);
            orderStatusHistory.setChangedAt(new Date());
            orderStatusHistoryRepository.save(orderStatusHistory);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                    .message("Change orderStatus from " + fromStatus + " to " + toStatus + " success!!!")
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message(ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

}
