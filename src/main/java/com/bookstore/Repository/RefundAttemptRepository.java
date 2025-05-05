package com.bookstore.Repository;

import com.bookstore.Entity.RefundAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundAttemptRepository extends JpaRepository<RefundAttempt, Long> {
    int countByOrderId(String orderId);
}
