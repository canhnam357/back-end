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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    @Transactional
    public ResponseEntity<GenericResponse> createOrder(Req_Create_Order orderDTO, String userId) {
        try {
            if (addressRepository.findByAddressId(orderDTO.getAddressId()).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found address!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (!Arrays.stream(PaymentMethod.values()).anyMatch(e -> e.name().equals(orderDTO.getPaymentMethod()))) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Payment method doesn't exists!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Cart cart = cartRepository.findByUserUserId(userId).get();

            if (cart.getCartItems().isEmpty()) {
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .message("Cart is empty!!!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            List<CartItem> cartItems = new ArrayList<>();
            boolean bad_request = false;
            for (CartItem cartItem : cart.getCartItems()) {
                Book book = bookRepository.findById(cartItem.getBook().getBookId()).get();
                if (book.getIsDeleted() || book.getInStock() == 0) {
                    bad_request = true;
                }
                else
                {
                    if (book.getInStock() < cartItem.getQuantity()) {
                        cartItem.setQuantity(book.getInStock());
                    }
                    cartItems.add(cartItem);
                }
            }

            if (bad_request) {
                cart.setCartItems(cartItems);
                cartRepository.save(cart);
                return ResponseEntity.status(400).body(GenericResponse.builder()
                        .message("Can't order quantity more than inStock, quantity has been reset by inStock!!!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }


            Orders order = new Orders();
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentMethod(PaymentMethod.valueOf(orderDTO.getPaymentMethod()));
            order.setAddress(addressRepository.findByAddressId(orderDTO.getAddressId()).get().getAddressInformation());
            order.setUser(userRepository.findById(userId).get());
            order.setTotalPrice(BigDecimal.ZERO);
            order = ordersRepository.save(order);

            for (CartItem cartItem : cart.getCartItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrders(order);
                orderItem.setBookName(cartItem.getBook().getBookName());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setTotalPrice(cartItem.getTotalPrice());
                orderItem.setUrlThumbnail(cartItem.getBook().getUrlThumbnail());
                orderItemRepository.save(orderItem);
                order.setTotalPrice(order.getTotalPrice().add(cartItem.getTotalPrice()));
            }
            cart.getCartItems().clear();
            cartRepository.save(cart);
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create Order successfully!!!")
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create Order failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(String userId, int page, int size) {
        try {
            Page<Orders> orders = ordersRepository.findAllByOrderByOrderAtDesc(PageRequest.of(page - 1, size));
            // not-completed
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Get all Orders success!!!")
                    .result(orders)
                    .statusCode(HttpStatus.OK.value())
                    .success(false)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get all Orders failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }



}
