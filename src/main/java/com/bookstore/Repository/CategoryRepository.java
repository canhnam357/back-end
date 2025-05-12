package com.bookstore.Repository;

import com.bookstore.Entity.Category;
import com.bookstore.Entity.Distributor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findAll();

    @Query("SELECT a FROM Category a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    List<Category> findListByNameContainingSubsequence(String keywords);

    @Query(value = """
        SELECT c.*
        FROM category c
        JOIN category_book cb ON c.category_id = cb.category_id
        JOIN book b ON cb.book_id = b.book_id
        WHERE b.is_deleted = FALSE
        GROUP BY c.category_id
        ORDER BY SUM(b.sold_quantity) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Category> findTopCategoriesBySoldQuantity(@Param("limit") int limit);
}
