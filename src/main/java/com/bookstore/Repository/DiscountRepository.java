package com.bookstore.Repository;

import com.bookstore.Entity.Book;
import com.bookstore.Entity.Discount;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, String> {
    @Query("SELECT d FROM Discount d ORDER BY d.createdAt DESC")
    Page<Discount> getAll(Pageable pageable);

    Optional<Discount> findByBook(Book book);
}
