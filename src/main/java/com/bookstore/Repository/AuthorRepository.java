package com.bookstore.Repository;

import com.bookstore.Entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, String> {
    Page<Author> findAll(Pageable pageable);
}
