package com.bookstore.Repository;

import com.bookstore.Entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {
    Page<Book> findAllByIsDeletedIsFalse(Pageable pageable);

    Page<Book> findAllByIsDeletedIsFalseAndNewArrivalIsTrue(Pageable pageable);
    Page<Book> findAll(Pageable pageable);

    Optional<Book> findByBookIdAndIsDeletedIsFalse(String bookId);
}
