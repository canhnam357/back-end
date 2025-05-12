package com.bookstore.Repository;

import com.bookstore.Entity.Review;
import com.bookstore.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {
    Page<Review> findAllByBookBookIdOrderByCreatedAtDesc(Pageable pageable, String bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.bookId = :bookId")
    BigDecimal findAverageRatingByBookId(@Param("bookId") String bookId);

    @Query("SELECT r FROM Review r WHERE r.book.bookId = :bookId AND r.rating = :rating ORDER BY r.createdAt DESC")
    Page<Review> findByBookIdAndRating(
            @Param("bookId") String bookId,
            @Param("rating") int rating,
            Pageable pageable
    );

    @Query("SELECT r.user FROM Review r WHERE r.reviewId = :reviewId")
    Optional<User> findUserByReviewId(@Param("reviewId") String reviewId);

    @Query("SELECT r FROM Review r " +
            "WHERE r.book.bookId = :bookId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findReviewsByBookIdOrderedByCreatedAtDesc(@Param("bookId") String bookId);
}
