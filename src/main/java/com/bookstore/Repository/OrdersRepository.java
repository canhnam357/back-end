package com.bookstore.Repository;

import com.bookstore.Constant.OrderStatus;
import com.bookstore.Constant.PaymentMethod;
import com.bookstore.Constant.PaymentStatus;
import com.bookstore.Entity.Book;
import com.bookstore.Entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, String> {
    Page<Orders> findAllByOrderByOrderAtDesc(Pageable pageable);

    Page<Orders> findByOrderStatusOrderByOrderAtDesc(OrderStatus status, Pageable pageable);

    Page<Orders> findAllByUserUserIdOrderByOrderAtDesc(String userId, Pageable pageable);

    Page<Orders> findAllByUserUserIdAndOrderStatusOrderByOrderAtDesc(String userId, OrderStatus status, Pageable pageable);
    List<Orders> findByPaymentMethodAndPaymentStatusAndExpireDatePaymentBefore(
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            Date now
    );

}
