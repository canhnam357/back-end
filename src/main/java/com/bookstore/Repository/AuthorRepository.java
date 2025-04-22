package com.bookstore.Repository;

import com.bookstore.Entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, String> {
    Page<Author> findAll(Pageable pageable);

    @Query("SELECT a FROM Author a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    Page<Author> findByNameContainingSubsequence(Pageable pageable, String keywords);

    @Query("SELECT a FROM Author a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    List<Author> findListByNameContainingSubsequence(String keywords);
}
