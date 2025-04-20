package com.bookstore.Repository;

import com.bookstore.Entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, String> {
    Page<Review> findAllByBookBookIdOrderByCreatedAtDesc(Pageable pageable, String bookId);
}
