package com.bookstore.Repository;

import com.bookstore.Entity.Book;
import com.bookstore.Entity.Distributor;
import com.bookstore.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {
    Page<Book> findAllByIsDeletedIsFalse(Pageable pageable);

    Page<Book> findAllByIsDeletedIsFalseAndNewArrivalIsTrue(Pageable pageable);
    Page<Book> findAll(Pageable pageable);

    Optional<Book> findByBookIdAndIsDeletedIsFalse(String bookId);

    Page<Book> findAllByAuthorAuthorId(Pageable pageable, String authorId);

    @Query("SELECT DISTINCT b.price FROM Book b WHERE b.isDeleted = false ORDER BY b.price ASC")
    List<BigDecimal> findAllDistinctPricesOrderByAsc();

    @Query("SELECT a FROM Book a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords)) ORDER BY a.createdAt DESC")
    Page<Book> findByNameContainingSubsequence(Pageable pageable, String keywords);
}
