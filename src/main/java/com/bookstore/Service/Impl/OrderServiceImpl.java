package com.bookstore.Service.Impl;

import com.bookstore.Constant.*;
import com.bookstore.DTO.*;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.OrderService;
import com.bookstore.Service.VNPayService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final BookRepository bookRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final VNPayService vnPayService;

    @Override
    @Transactional
    public ResponseEntity<GenericResponse> createOrder(Req_Create_Order orderDTO, String authorizationHeader) {
        try {
            log.info("Bắt đầu tạo đơn hàng!");
            String token = authorizationHeader.substring(7);
            String userId = jwtTokenProvider.getUserIdFromJwt(token);

            if (userRepository.findByUserIdAndActiveIsTrue(userId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Tài khoản không tồn tại hoặc đang bị khoá!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            if (addressRepository.findByAddressId(orderDTO.getAddressId()).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Địa chỉ!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (Arrays.stream(PaymentMethod.values()).noneMatch(e -> e.name().equals(orderDTO.getPaymentMethod()))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Phương thức thanh toán!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            assert (cartRepository.findByUserUserId(userId).isPresent());
            Cart cart = cartRepository.findByUserUserId(userId).get();

            if (cart.getCartItems().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Giỏ hàng đang rỗng!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            ZonedDateTime cutoffDate = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusHours(8);

            int LIMIT = 10;

            if (ordersRepository.countCancelledOrdersWithinTime(userId, OrderStatus.CANCELLED, cutoffDate) >= LIMIT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .message("Bạn đã huỷ đơn / thanh toán thất bại nhiều đơn hàng gần đây. Vui lòng chờ thêm vài tiếng trước khi đặt thêm đơn hàng mới!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .success(false)
                        .build());
            }

            int PENDING = 30;

            if (ordersRepository.countPendingOrder(userId, OrderStatus.PENDING, PaymentStatus.PENDING) >= PENDING) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .message("Bạn có nhiều đơn hàng đang chờ Thanh toán, vui lòng thanh toán trước khi đặt thêm đơn hàng!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .success(false)
                        .build());
            }

            List<CartItem> cartItems = new ArrayList<>();
            boolean bad_request = false;
            for (CartItem cartItem : cart.getCartItems()) {
                assert (bookRepository.findById(cartItem.getBook().getBookId()).isPresent());
                Book book = bookRepository.findById(cartItem.getBook().getBookId()).get();
                if (book.isDeleted() || book.getInStock() == 0) {
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
                        .message("Đơn hàng có sản phẩm có số lượng lớn hơn số lượng tồn kho, số lượng đã được đặt lại bằng số lượng tồn kho!")
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
                        .message("Phải có ít nhất 1 quyển sách trong Đơn hàng!")
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
            assert (userRepository.findById(userId).isPresent());
            order.setUser(userRepository.findById(userId).get());
            order.setTotalPrice(BigDecimal.ZERO);
            order.setRefundStatus(RefundStatus.NONE);
            order = ordersRepository.save(order);

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            for (CartItem cartItem : orderCartItem) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrders(order);
                orderItem.setPrice(cartItem.getBook().getPrice());
                orderItem.setBookId(cartItem.getBook().getBookId());
                orderItem.setBookName(cartItem.getBook().getBookName());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setTotalPrice(cartItem.getTotalPrice());
                orderItem.setUrlThumbnail(cartItem.getBook().getUrlThumbnail());
                assert (bookRepository.findById(cartItem.getBook().getBookId()).isPresent());
                Book book = bookRepository.findById(cartItem.getBook().getBookId()).get();
                if (book.getDiscount() != null && book.getDiscount().getStartDate().isBefore(now) && book.getDiscount().getEndDate().isAfter(now)) {
                    if (book.getDiscount().getDiscountType() == DiscountType.FIXED) {
                        orderItem.setPriceAfterSales(book.getPrice().subtract(book.getDiscount().getDiscount()));
                    }
                    else {
                        orderItem.setPriceAfterSales(book.getPrice().multiply(BigDecimal.valueOf(100L).subtract(book.getDiscount().getDiscount())).divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP));
                    }
                    orderItem.setTotalPrice(orderItem.getPriceAfterSales().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                }
                book.setInStock(book.getInStock() - cartItem.getQuantity());
                orderItemRepository.save(orderItem);
                order.getOrderDetails().add(orderItem);
                order.setTotalPrice(order.getTotalPrice().add(orderItem.getTotalPrice()));
            }
            cart.getCartItems().clear();

            for (CartItem cartItem : newCartItems) {
                cartItem.setCart(cart);
                cart.getCartItems().add(cartItem);
            }
            cartRepository.save(cart);
            log.info("Tạo đơn hàng thành công!");
            return ResponseEntity.status(HttpStatus.CREATED).body(GenericResponse.builder()
                    .message("Tạo Đơn hàng thành công!")
                    .result(order.getOrderId())
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Tạo đơn hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
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

            List<Res_Get_OrderOfUser> res = new ArrayList<>();
            for (Orders order : orders) {
                res.add(new Res_Get_OrderOfUser(
                        order.getOrderId(),
                        order.getOrderStatus().name(),
                        order.getPaymentMethod().name(),
                        order.getPaymentStatus().name(),
                        order.getUser().getFullName(),
                        order.getAddress(),
                        order.getPhoneNumber(),
                        order.getOrderAt(),
                        order.getTotalPrice(),
                        order.getRefundStatus().name(),
                        order.getRefundAt(),
                        order.getRefundTimesRemain(),
                        order.getPaymentUrl()
                ));
            }

            Page<Res_Get_OrderOfUser> dtoPage = new PageImpl<>(res, orders.getPageable(), orders.getTotalElements());


            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách đơn hàng thành công!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách đơn hàng của người dùng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> orderDetail(String userId, String orderId) {
        try {
            assert (userRepository.findById(userId).isPresent());
            User user = userRepository.findById(userId).get();

            if (ordersRepository.findById(orderId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Đơn hàng!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            Orders order = ordersRepository.findById(orderId).get();

            if (!userId.equals(order.getUser().getUserId()) && user.getRole() == Role.USER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Người dùng không có quyền xem chi tiết đơn hàng này!")
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
                        ele.getPrice(),
                        ele.getPriceAfterSales(),
                        ele.getQuantity(),
                        ele.getTotalPrice(),
                        ele.getUrlThumbnail()
                ));
            }

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy chi tiết đơn hàng thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(res)
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Lấy chi tiết đơn hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
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
                        order.getUser().getFullName(),
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
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách đơn hàng thành công!!!")
                    .statusCode(HttpStatus.OK.value())
                    .result(dtoPage)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách đơn hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
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
            list.add("FAILED_DELIVERY");
            if (list.contains(orderStatus)) {
                orders = ordersRepository.findAllByOrderStatusOrderByOrderAtDesc(OrderStatus.valueOf(orderStatus), PageRequest.of(index - 1, size));
            }
            else {
                List<OrderStatus> orderStatusList = List.of(OrderStatus.READY_TO_SHIP, OrderStatus.DELIVERING, OrderStatus.DELIVERED, OrderStatus.FAILED_DELIVERY);
                orders = ordersRepository.findAllByOrderStatusInOrderByOrderAtDesc(orderStatusList, PageRequest.of(index - 1, size));
            }

            List<Res_Get_Order> res = new ArrayList<>();
            for (Orders order : orders) {
                res.add(new Res_Get_Order(
                        order.getOrderId(),
                        order.getOrderStatus().name(),
                        order.getPaymentMethod().name(),
                        order.getPaymentStatus().name(),
                        order.getUser().getFullName(),
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
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách đơn hàng thành công!")
                    .statusCode(HttpStatus.OK.value())
                    .result(dtoPage)
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách đơn hàng cho Shipper thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getMonthlyRevenue(int year) {
        try {
            List<Object[]> rawResult = ordersRepository.findMonthlyRevenueByYear(year);
            Map<Integer, BigDecimal> result = new HashMap<>();

            // Khởi tạo mặc định 12 tháng = 0
            for (int month = 1; month <= 12; month++) {
                result.put(month, BigDecimal.ZERO);
            }

            // Ghi đè các tháng có dữ liệu
            for (Object[] row : rawResult) {
                int month = ((Number) row[0]).intValue();
                BigDecimal total = (BigDecimal) row[1];
                result.put(month, total);
            }

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy doanh thu của năm thành công!")
                    .result(result)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Lấy thống kê doanh thu của tháng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getOrderCountByStatus() {
        try {
            List<Object[]> rawResult = ordersRepository.countOrdersByStatus();
            Map<OrderStatus, Long> result = new EnumMap<>(OrderStatus.class);

            // Khởi tạo tất cả orderStatus = 0
            for (OrderStatus status : OrderStatus.values()) {
                result.put(status, 0L);
            }

            // Ghi đè những status có dữ liệu thực
            for (Object[] row : rawResult) {
                OrderStatus status = (OrderStatus) row[0];
                Long count = (Long) row[1];
                result.put(status, count);
            }

            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy thống kê đơn hàng thành công!")
                    .result(result)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            log.error("Lấy thống kê số lượng đơn hàng thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
