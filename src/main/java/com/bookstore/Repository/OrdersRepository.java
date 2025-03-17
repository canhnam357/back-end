package com.bookstore.Repository;

import com.bookstore.Entity.Book;
import com.bookstore.Entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, String> {
    Page<Orders> findAllByOrderByOrderAtDesc(Pageable pageable);
}
