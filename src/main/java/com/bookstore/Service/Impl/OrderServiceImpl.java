package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.Constant.RefundStatus;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Create_Order;
import com.bookstore.DTO.Res_Get_Order;
import com.bookstore.DTO.Res_Get_OrderDetail;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Override
    @Transactional
    public ResponseEntity<GenericResponse> createOrder(Req_Create_Order orderDTO, String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(token);

            if (userRepository.findByUserIdAndIsActiveIsTrue(userId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("User not found or not active!!!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

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
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Cart is empty!!!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
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
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .message("Cart have item that quantity more than inStock, quantity has been reset by inStock!!!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .success(false)
                        .build());
            }

            List<CartItem> orderCartItem = new ArrayList<>();
            List<CartItem> newCartItems = new ArrayList<>();

            for (CartItem cartItem : cart.getCartItems()) {
                if (orderDTO.getBookIds().contains(cartItem.getBook().getBookId())) {
                    orderCartItem.add(cartItem);
                }
                else {
                    newCartItems.add(cartItem);
                }
            }

            if (orderCartItem.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Must have at least 1 book in order!!!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            Orders order = new Orders();
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentMethod(PaymentMethod.valueOf(orderDTO.getPaymentMethod()));
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setAddress(addressRepository.findByAddressId(orderDTO.getAddressId()).get().getAddressInformation());
            order.setPhoneNumber(addressRepository.findByAddressId(orderDTO.getAddressId()).get().getPhoneNumber());
            order.setUser(userRepository.findById(userId).get());
            order.setTotalPrice(BigDecimal.ZERO);
            order.setRefundStatus(RefundStatus.NONE);
            order = ordersRepository.save(order);


            for (CartItem cartItem : orderCartItem) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrders(order);
                orderItem.setBookId(cartItem.getBook().getBookId());
                orderItem.setBookName(cartItem.getBook().getBookName());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setTotalPrice(cartItem.getTotalPrice());
                orderItem.setUrlThumbnail(cartItem.getBook().getUrlThumbnail());
                Book book = bookRepository.findById(cartItem.getBook().getBookId()).get();
                book.setInStock(book.getInStock() - cartItem.getQuantity());
                orderItemRepository.save(orderItem);
                order.getOrderDetails().add(orderItem);
                order.setTotalPrice(order.getTotalPrice().add(cartItem.getTotalPrice()));
            }
            cart.getCartItems().clear();

            for (CartItem cartItem : newCartItems) {
                cartItem.setCart(cart);
                cart.getCartItems().add(cartItem);
            }
            cartRepository.save(cart);

            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create Order successfully!!!")
                    .result(order.getOrderId())
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
    public ResponseEntity<GenericResponse> getAllOfUser(String userId, String orderStatus, int page, int size) {
        try {
            Page<Orders> orders;
            if (Arrays.stream(OrderStatus.values()).anyMatch(e -> e.name().equals(orderStatus))) {
                orders = ordersRepository.findAllByUserUserIdAndOrderStatusOrderByOrderAtDesc(userId, OrderStatus.valueOf(orderStatus), PageRequest.of(page - 1, size));
            }
            else {
                orders = ordersRepository.findAllByUserUserIdOrderByOrderAtDesc(userId, PageRequest.of(page - 1, size));
            }

            List<Res_Get_Order> res = new ArrayList<>();
            for (Orders order : orders) {
                res.add(new Res_Get_Order(
                        order.getOrderId(),
                        order.getOrderStatus().name(),
                        order.getPaymentMethod().name(),
                        order.getPaymentStatus().name(),
                        order.getAddress(),
                        order.getPhoneNumber(),
                        order.getOrderAt(),
                        order.getTotalPrice(),
                        order.getRefundStatus().name(),
                        order.getRefundAt(),
                        order.getRefundTimesRemain()
                ));
            }

            Page<Res_Get_Order> dtoPage = new PageImpl<>(res, orders.getPageable(), orders.getTotalElements());


            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Get all Orders success!!!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
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

    @Override
    public ResponseEntity<GenericResponse> orderDetail(String userId, String orderId) {
        try {
            User user = userRepository.findById(userId).get();

            if (ordersRepository.findById(orderId).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found Order!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Orders order = ordersRepository.findById(orderId).get();

            if (userId != order.getUser().getUserId() && user.getRole().getName().equals("USER")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("User doesn't have permission to see this Order!!!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            List<Res_Get_OrderDetail> res = new ArrayList<>();
            for (OrderItem ele : order.getOrderDetails()) {
                res.add(new Res_Get_OrderDetail(
                        ele.getOrderDetailId(),
                        ele.getBookId(),
                        ele.getBookName(),
                        ele.getQuantity(),
                        ele.getTotalPrice(),
                        ele.getUrlThumbnail()
                ));
            }

            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Get Order Detail successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .result(res)
                    .success(true)
                    .build());

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get Order Detail failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(String orderStatus, int index, int size) {
        try {
            Page<Orders> orders;
            if (Arrays.stream(OrderStatus.values()).anyMatch(e -> e.name().equals(orderStatus))) {
                orders = ordersRepository.findAllByOrderStatusOrderByOrderAtDesc(OrderStatus.valueOf(orderStatus), PageRequest.of(index - 1, size));
            }
            else {
                orders = ordersRepository.findAllByOrderByOrderAtDesc(PageRequest.of(index - 1, size));
            }

            List<Res_Get_Order> res = new ArrayList<>();
            for (Orders order : orders) {
                res.add(new Res_Get_Order(
                        order.getOrderId(),
                        order.getOrderStatus().name(),
                        order.getPaymentMethod().name(),
                        order.getPaymentStatus().name(),
                        order.getAddress(),
                        order.getPhoneNumber(),
                        order.getOrderAt(),
                        order.getTotalPrice(),
                        order.getRefundStatus().name(),
                        order.getRefundAt(),
                        order.getRefundTimesRemain()
                ));
            }
            Page<Res_Get_Order> dtoPage = new PageImpl<>(res, orders.getPageable(), orders.getTotalElements());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Get All Order successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .result(dtoPage)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Order failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllForShipper(String orderStatus, int index, int size) {
        try {
            Page<Orders> orders;
            List<String> list = new ArrayList<>();
            list.add("READY_TO_SHIP");
            list.add("DELIVERING");
            list.add("DELIVERED");
            if (list.contains(orderStatus)) {
                orders = ordersRepository.findAllByOrderStatusOrderByOrderAtDesc(OrderStatus.valueOf(orderStatus), PageRequest.of(index - 1, size));
            }
            else {
                List<OrderStatus> orderStatusList = List.of(OrderStatus.READY_TO_SHIP, OrderStatus.DELIVERING, OrderStatus.DELIVERED);
                orders = ordersRepository.findAllByOrderStatusInOrderByOrderAtDesc(orderStatusList, PageRequest.of(index - 1, size));
            }

            List<Res_Get_Order> res = new ArrayList<>();
            for (Orders order : orders) {
                res.add(new Res_Get_Order(
                        order.getOrderId(),
                        order.getOrderStatus().name(),
                        order.getPaymentMethod().name(),
                        order.getPaymentStatus().name(),
                        order.getAddress(),
                        order.getPhoneNumber(),
                        order.getOrderAt(),
                        order.getTotalPrice(),
                        order.getRefundStatus().name(),
                        order.getRefundAt(),
                        order.getRefundTimesRemain()
                ));
            }
            Page<Res_Get_Order> dtoPage = new PageImpl<>(res, orders.getPageable(), orders.getTotalElements());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Get All Order successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .result(dtoPage)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Order failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }


}
