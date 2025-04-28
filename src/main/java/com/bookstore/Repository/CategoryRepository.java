package com.bookstore.Repository;

import com.bookstore.Entity.Category;
import com.bookstore.Entity.Distributor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findAll();

    @Query("SELECT a FROM Category a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    List<Category> findListByNameContainingSubsequence(String keywords);
}
