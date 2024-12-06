package com.bookstore.Repository;

import com.bookstore.Entity.Author;
import com.bookstore.Entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, String> {
    Page<Publisher> findAll(Pageable pageable);
}
