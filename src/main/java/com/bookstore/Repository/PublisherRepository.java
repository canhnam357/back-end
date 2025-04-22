package com.bookstore.Repository;

import com.bookstore.Entity.Author;
import com.bookstore.Entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, String> {
    Page<Publisher> findAll(Pageable pageable);

    @Query("SELECT a FROM Publisher a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    Page<Publisher> findByNameContainingSubsequence(Pageable pageable, String keywords);
    @Query("SELECT a FROM Publisher a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    List<Publisher> findListByNameContainingSubsequence(String keywords);
}
