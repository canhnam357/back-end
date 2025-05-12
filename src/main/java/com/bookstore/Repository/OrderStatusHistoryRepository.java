package com.bookstore.Repository;

import com.bookstore.Entity.OrderStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {
    Page<OrderStatusHistory> findByOrderOrderIdOrderByChangedAtDesc(String orderId, Pageable pageable);
    Page<OrderStatusHistory> findAllByOrderByChangedAtDesc(Pageable pageable);
}
