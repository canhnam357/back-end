package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Order;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional
    public ResponseEntity<GenericResponse> createOrder(Req_Create_Order orderDTO, String userId) {
        try {
            int number_of_cart_item = cartItemRepository.countByCartUserUserId(userId);
            if (number_of_cart_item == 0) {
                return ResponseEntity.badRequest().body(
                        GenericResponse.builder()
                                .message("Cart is empty or User not exists!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build()
                );
            }
            Orders order = new Orders();
            order.setUser(userRepository.findById(userId).get());
            order.setOrderStatus(OrderStatus.PENDING);
            try {
                order.setPaymentMethod(PaymentMethod.valueOf(orderDTO.getPaymentMethod()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                        GenericResponse.builder()
                                .message("Payment Method Invalid!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build()
                );
            }
            try {
                order.setAddress(addressRepository.findByAddressId(orderDTO.getAddressId()).get());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(
                        GenericResponse.builder()
                                .message("Not found Address!")
                                .result(null)
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build()
                );
            }
            order.setTotalPrice(BigDecimal.ZERO);
            ordersRepository.save(order);
            List<CartItem> cartItems = cartItemRepository.findAllByCartUserUserId(userId);
            for (CartItem cartItem : cartItems) {
                OrderItem insert_orderItem = new OrderItem();
                insert_orderItem.setQuantity(cartItem.getQuantity());
                insert_orderItem.setTotalPrice(cartItem.getTotalPrice());
                insert_orderItem.setBookName(cartItem.getBook().getBookName());
                insert_orderItem.setOrders(ordersRepository.findById(order.getOrderId()).get());
                insert_orderItem.setUrlThumbnail(cartItem.getBook().getUrlThumbnail());
                order.setTotalPrice(order.getTotalPrice().add(insert_orderItem.getTotalPrice()));
                orderItemRepository.save(insert_orderItem);
            }
            cartItemRepository.deleteAllByCartUserUserId(userId);
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Create Order Successfully!")
                            .result(order)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(
                    GenericResponse.builder()
                            .message("Create Order failed!")
                            .result(null)
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(String userId, int page, int size) {
        try {
            Page<Orders> orders = ordersRepository.findAllByOrderByOrderAtDesc(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Order Successfully!")
                            .result(orders)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Orders failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
