package com.bookstore.Repository;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN oi.orders o " +
            "WHERE oi.bookId = :bookId " +
            "AND o.orderStatus = :orderStatus " +
            "ORDER BY o.orderAt DESC")
    List<OrderItem> findOrderItemByBookIdAndStatus(
            @Param("bookId") String bookId,
            @Param("orderStatus") OrderStatus orderStatus);
}
