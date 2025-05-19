package com.bookstore.Service.Impl;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.Constant.Role;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_ChangeOrderStatus;
import com.bookstore.DTO.Res_Get_OrderStatusHistory;
import com.bookstore.Entity.*;
import com.bookstore.Repository.BookRepository;
import com.bookstore.Repository.OrderStatusHistoryRepository;
import com.bookstore.Repository.OrdersRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.OrderStatusHistoryService;
import com.bookstore.Utils.CheckCanChangeOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final BookRepository bookRepository;

    private boolean EMPLOYEEPermission(Role role) {
        return role != Role.ADMIN && role != Role.EMPLOYEE;
    }

    private boolean SHIPPERPermission(Role role) {
        return role != Role.ADMIN && role != Role.SHIPPER;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "mostPurchasedBooks", allEntries = true),
            @CacheEvict(value = "mostPurchasedCategories", allEntries = true)
    })
    public ResponseEntity<GenericResponse> changeOrderStatus(String userId, Req_ChangeOrderStatus reqChangeOrderStatus) {
        try {
            log.info("Bắt đầu thay đổi trạng thái đơn hàng!");
            if (reqChangeOrderStatus.getOrderId() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("OrderId không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            String orderId = reqChangeOrderStatus.getOrderId();

            if (ordersRepository.findById(orderId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Không tìm thấy Đơn hàng!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (reqChangeOrderStatus.getFromStatus() == null || reqChangeOrderStatus.getToStatus() == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("fromStatus và toStatus không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            String fromStatus = reqChangeOrderStatus.getFromStatus();
            String toStatus = reqChangeOrderStatus.getToStatus();

            Orders order = ordersRepository.findById(orderId).get();
            if (Arrays.stream(OrderStatus.values()).noneMatch(e -> e.name().equals(fromStatus)) || Arrays.stream(OrderStatus.values()).noneMatch(e -> e.name().equals(toStatus))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message(fromStatus + " hoặc " + toStatus + " không nằm trong danh sách trạng thái Đơn hàng!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (!CheckCanChangeOrderStatus.check(fromStatus, toStatus)) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Không thể thay đổi trạng thái Đơn hàng từ " + fromStatus + " -> " + toStatus + "!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            // User CANCELLED Order
            if (toStatus.equals(OrderStatus.CANCELLED.name()) && (!order.getUser().getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Người dùng không thể Huỷ đơn hàng không phải của họ!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            if (toStatus.equals(OrderStatus.CANCELLED.name()) && order.getPaymentMethod().equals(PaymentMethod.CARD) && order.getPaymentStatus().equals(PaymentStatus.PENDING)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Không thể chuyển Đơn hàng sang CANCELLED khi trạng thái thanh toán vẫn đang PENDING, chờ 30 phút sau và thử lại!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            assert (userRepository.findById(userId).isPresent());

            User user = userRepository.findById(userId).get();

            if (order.getPaymentMethod().equals(PaymentMethod.CARD) && order.getPaymentStatus().equals(PaymentStatus.PENDING) && toStatus.equals(OrderStatus.REJECTED.name())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Không thể chuyển Đơn hàng sang REJECTED khi trạng thái thanh toán vẫn đang PENDING!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            if (order.getPaymentMethod().equals(PaymentMethod.CARD) && order.getPaymentStatus().equals(PaymentStatus.PENDING) && toStatus.equals(OrderStatus.IN_PREPARATION.name())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .message("Không thể chuyển Đơn hàng sang IN_PREPARATION khi trạng thái thanh toán vẫn đang PENDING!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .success(false)
                        .build());
            }

            // PENDING -> REJECTED
            if (EMPLOYEEPermission(user.getRole()) && toStatus.equals(OrderStatus.REJECTED.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Phải có quyền EMPLOYEE để thay đổi đơn hàng từ PENDING -> REJECTED!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // PENDING -> IN_PREPARATION
            if (EMPLOYEEPermission(user.getRole()) && toStatus.equals(OrderStatus.IN_PREPARATION.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Phải có quyền EMPLOYEE để thay đổi đơn hàng từ PENDING -> IN_PREPARATION!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // IN_PREPARATION -> READY_TO_SHIP
            if (EMPLOYEEPermission(user.getRole()) && toStatus.equals(OrderStatus.READY_TO_SHIP.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Phải có quyền EMPLOYEE để thay đổi đơn hàng từ IN_PREPARATION -> READY_TO_SHIP!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // FAILED_DELIVERY -> RETURNED
            if (EMPLOYEEPermission(user.getRole()) && toStatus.equals(OrderStatus.RETURNED.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Phải có quyền EMPLOYEE để thay đổi đơn hàng từ FAILED_DELIVERY -> RETURNED!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // READY_TO_SHIP -> DELIVERING
            if (SHIPPERPermission(user.getRole()) && toStatus.equals(OrderStatus.DELIVERING.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Phải có quyền SHIPPER để thay đổi Đơn hàng từ READY_TO_SHIP -> DELIVERING!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // DELIVERING -> DELIVERED
            if (SHIPPERPermission(user.getRole()) && toStatus.equals(OrderStatus.DELIVERED.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Phải có quyền EMPLOYEE để thay đổi đơn hàng từ DELIVERING -> DELIVERED!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            // DELIVERING -> FAILED_DELIVERY
            if (SHIPPERPermission(user.getRole()) && toStatus.equals(OrderStatus.FAILED_DELIVERY.name())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("Phải có quyền SHIPPER để thay đổi Đơn hàng từ DELIVERING -> FAILED_DELIVERY!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }

            order.setOrderStatus(OrderStatus.valueOf(toStatus));
            order = ordersRepository.save(order);

            // Hoàn lại tồn kho khi đơn hàng bị REJECTED, CANCELLED, hoặc RETURNED
            if (toStatus.equals("REJECTED") || toStatus.equals("CANCELLED") || toStatus.equals("RETURNED")) {
                for (OrderItem orderItem : order.getOrderDetails()) {
                    int updatedRows = bookRepository.updateInStockAdd(orderItem.getBookId(), orderItem.getQuantity());
                    if (updatedRows == 0) {
                        throw new IllegalStateException("Không thể cập nhật tồn kho cho sách ID: " + orderItem.getBookId());
                    }
                }
            }

            // Cập nhật số lượng đã bán khi đơn hàng DELIVERED
            if (toStatus.equals("DELIVERED")) {
                order.setPaymentStatus(PaymentStatus.SUCCESS);
                ordersRepository.save(order);
                for (OrderItem orderItem : order.getOrderDetails()) {
                    int updatedRows = bookRepository.updateSoldQuantity(orderItem.getBookId(), orderItem.getQuantity());
                    if (updatedRows == 0) {
                        throw new IllegalStateException("Không thể cập nhật số lượng đã bán cho sách ID: " + orderItem.getBookId());
                    }
                }
            }

            OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
            orderStatusHistory.setOrder(order);
            orderStatusHistory.setFromStatus(OrderStatus.valueOf(fromStatus));
            orderStatusHistory.setToStatus(OrderStatus.valueOf(toStatus));
            orderStatusHistory.setChangedBy(user);
            if (toStatus.equals("REJECTED") || toStatus.equals("CANCELLED") || toStatus.equals("FAILED_DELIVERY")) {
                orderStatusHistory.setCause(reqChangeOrderStatus.getCause());
            }
            orderStatusHistory.setChangedAt(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            orderStatusHistoryRepository.save(orderStatusHistory);
            log.info("Thay đổi trạng thái đơn hàng thành công, từ " + fromStatus + " -> " + toStatus);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                    .message("Thay đổi trạng thái Đơn hàng từ " + fromStatus + " sang " + toStatus + " thành công!")
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Thay đổi trạng thái đơn hàng thất bại, lỗi: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống: " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getAll(int index, int size) {
        try {
            Page<OrderStatusHistory> orderStatusHistories = orderStatusHistoryRepository.findAllByOrderByChangedAtDesc(PageRequest.of(index - 1, size));
            List<Res_Get_OrderStatusHistory> res = new ArrayList<>();
            for (OrderStatusHistory orderStatusHistory : orderStatusHistories) {
                Res_Get_OrderStatusHistory temp = new Res_Get_OrderStatusHistory();
                temp.convert(orderStatusHistory);
                res.add(temp);
            }
            Page<Res_Get_OrderStatusHistory> dtoPage = new PageImpl<>(res, orderStatusHistories.getPageable(), orderStatusHistories.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách Lịch sử thay đổi trạng thái đơn hàng thành công!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách trạng thái đơn hàng thất bại, lỗi: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống: " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<GenericResponse> getByOrder(String orderId, int index, int size) {
        try {
            Page<OrderStatusHistory> orderStatusHistories;
            if (!orderId.isEmpty()) {
                orderStatusHistories = orderStatusHistoryRepository.findByOrderOrderIdOrderByChangedAtDesc(orderId, PageRequest.of(index - 1, size));
            } else {
                orderStatusHistories = orderStatusHistoryRepository.findAllByOrderByChangedAtDesc(PageRequest.of(index - 1, size));
            }
            List<Res_Get_OrderStatusHistory> res = new ArrayList<>();
            for (OrderStatusHistory orderStatusHistory : orderStatusHistories) {
                Res_Get_OrderStatusHistory temp = new Res_Get_OrderStatusHistory();
                temp.convert(orderStatusHistory);
                res.add(temp);
            }
            Page<Res_Get_OrderStatusHistory> dtoPage = new PageImpl<>(res, orderStatusHistories.getPageable(), orderStatusHistories.getTotalElements());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Lấy danh sách Lịch sử thay đổi trạng thái đơn hàng thành công!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            log.error("Lấy danh sách trạng thái đơn hàng theo OrderId thất bại, lỗi: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .message("Lỗi hệ thống: " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}