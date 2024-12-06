package com.bookstore.Repository;

import com.bookstore.Entity.Author;
import com.bookstore.Entity.BookType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTypeRepository extends JpaRepository<BookType, String> {
    Page<BookType> findAll(Pageable pageable);
}
