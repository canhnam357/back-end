package com.bookstore.Repository;

import com.bookstore.Entity.Cart;
import com.bookstore.Entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUserUserId(String userId);

}
