package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
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

    @Transactional
    @Override
    public ResponseEntity<GenericResponse> changeOrderStatus(String userId, String orderId, String fromStatus, String toStatus) {
        try {
            if (userRepository.findById(userId).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found User!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            User user = userRepository.findById(userId).get();

            if (!CheckCanChangeOrderStatus.check(user.getRole().getName(), fromStatus, toStatus)) {
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .message("User doesn't have permission to change this OrderStatus!!!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            if (ordersRepository.findById(orderId).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found Order!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Orders orders = ordersRepository.findById(orderId).get();

            if (!orders.getOrderStatus().name().equals(fromStatus)) {
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .message("Wrong current OrderStatus!!!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            if (toStatus.equals("REJECTED") && orders.getPaymentStatus().name().equals("PENDING") && orders.getExpireDatePayment().before(new Date())) {
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .message("Cannot REJECT order until ExpireDatePayment has arrived!!!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            if (toStatus.equals("IN_PREPARATION") && orders.getPaymentStatus().name().equals("PENDING") && orders.getExpireDatePayment().before(new Date())) {
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .message("Cannot IN_PREPARATION order until ExpireDatePayment has arrived!!!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            System.err.println(OrderStatus.valueOf(fromStatus));
            System.err.println(OrderStatus.valueOf(toStatus));

            orders.setOrderStatus(OrderStatus.valueOf(toStatus));
            orders = ordersRepository.save(orders);

            if (toStatus.equals("REJECTED") || toStatus.equals("CANCELLED")) {
                for (OrderItem orderItem : orders.getOrderDetails()) {
                    Book book = bookRepository.findById(orderItem.getBookId()).get();
                    book.setInStock(book.getInStock() + orderItem.getQuantity());
                    bookRepository.save(book);
                }
            }

            OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
            orderStatusHistory.setOrder(orders);
            orderStatusHistory.setFromStatus(OrderStatus.valueOf(fromStatus));
            orderStatusHistory.setToStatus(OrderStatus.valueOf(toStatus));
            orderStatusHistory.setChangedBy(user);
            orderStatusHistory.setChangedAt(new Date());
            orderStatusHistoryRepository.save(orderStatusHistory);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Change OrderStatus successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Change OrderStatus failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
