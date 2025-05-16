package com.bookstore.Repository;

import com.bookstore.Entity.RefundAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundAttemptRepository extends JpaRepository<RefundAttempt, Long> {
    int countByOrderOrderId(String orderId);

    Page<RefundAttempt> findByOrderOrderIdOrderByAttemptTimeDesc(String orderId, Pageable pageable);

    Page<RefundAttempt> findAllByOrderByAttemptTimeDesc(Pageable pageable);
}
