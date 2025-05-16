package com.bookstore.Repository;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.Constant.RefundStatus;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, String> {
    Page<Orders> findAllByOrderByOrderAtDesc(Pageable pageable);

    Page<Orders> findAllByOrderStatusOrderByOrderAtDesc(OrderStatus status, Pageable pageable);

    Page<Orders> findAllByUserUserIdOrderByOrderAtDesc(String userId, Pageable pageable);

    Page<Orders> findAllByOrderStatusInOrderByOrderAtDesc(List<OrderStatus> statusList, Pageable pageable);


    Page<Orders> findAllByUserUserIdAndOrderStatusOrderByOrderAtDesc(String userId, OrderStatus status, Pageable pageable);
    List<Orders> findByPaymentMethodAndPaymentStatusAndExpireDatePaymentBefore(
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            ZonedDateTime now
    );

    @Query("SELECT o FROM Orders o WHERE " +
            "o.orderStatus IN (:cancelled, :returned, :rejected) AND " +
            "o.paymentMethod = :paymentMethod AND " +
            "o.paymentStatus = :paymentStatus AND " +
            "o.refundTimesRemain > 0 AND " +
            "o.refundStatus NOT IN (:pendingRefund, :refunded) AND " +
            "(o.lastCallRefund IS NULL OR o.lastCallRefund <= :oneHourAgo)")
    List<Orders> findOrdersForRefund(
            OrderStatus cancelled,
            OrderStatus returned,
            OrderStatus rejected,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            RefundStatus pendingRefund,
            RefundStatus refunded,
            ZonedDateTime oneHourAgo);

    @Query("SELECT COUNT(o) FROM Orders o " +
            "WHERE o.orderStatus = :status " +
            "AND o.orderAt >= :cutoffTime")
    long countCancelledOrdersWithinTime(
            @Param("status") OrderStatus status,
            @Param("cutoffTime") ZonedDateTime cutoffTime
    );

    @Query(value = "SELECT MONTH(order_at) AS month, COALESCE(SUM(total_price), 0) AS total " +
            "FROM orders " +
            "WHERE order_status = 'DELIVERED' " +
            "AND YEAR(order_at) = :year " +
            "GROUP BY MONTH(order_at)", nativeQuery = true)
    List<Object[]> findMonthlyRevenueByYear(@Param("year") int year);

    @Query("SELECT o.orderStatus, COUNT(o) FROM Orders o GROUP BY o.orderStatus")
    List<Object[]> countOrdersByStatus();

}
