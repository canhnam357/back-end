package com.bookstore.Repository;

import com.bookstore.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findAllByCartUserUserId(String userId);

    Optional<CartItem> findByBookBookIdAndCartUserUserId (String cartItemId, String userId);
}
