package com.bookstore.Repository;

import com.bookstore.Entity.Book;
import com.bookstore.Entity.Distributor;
import com.bookstore.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {
    Page<Book> findAllByIsDeletedIsFalse(Pageable pageable);

    List<Book> findAllByIsDeletedIsFalseAndNewArrivalIsTrue();
    Page<Book> findAll(Pageable pageable);

    Optional<Book> findByBookIdAndIsDeletedIsFalse(String bookId);

    Page<Book> findAllByAuthorAuthorId(Pageable pageable, String authorId);

    @Query("SELECT DISTINCT b.price FROM Book b WHERE b.isDeleted = false ORDER BY b.price ASC")
    List<BigDecimal> findAllDistinctPricesOrderByAsc();

    @Query("SELECT a FROM Book a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords)) ORDER BY a.createdAt DESC")
    Page<Book> findByNameContainingSubsequence(Pageable pageable, String keywords);

    @Query(value = "SELECT * FROM book WHERE (:keywords IS NULL OR LOWER(name_normalized) LIKE LOWER(:keywords)) AND is_deleted IS FALSE ORDER BY sold_quantity DESC LIMIT :lim",
            nativeQuery = true)
    List<Book> search(@Param("keywords") String keywords, @Param("lim") int lim);

    @Query("SELECT b FROM Book b " +
            "WHERE b.isDeleted = false " +
            "AND b.discount IS NOT NULL " +
            "AND b.discount.startDate <= :now " +
            "AND b.discount.endDate >= :now")
    List<Book> findBooksWithActiveDiscount(Date now);

    @Query(value = """
        SELECT b.* FROM book b
        LEFT JOIN review r ON b.book_id = r.book_id
        WHERE b.is_deleted = false
        GROUP BY b.book_id
        ORDER BY COALESCE(AVG(r.rating), 0) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Book> findTopBooksByAverageRating(@Param("limit") int limit);

    @Query(value = """
        SELECT * FROM book 
        WHERE is_deleted = false 
        ORDER BY sold_quantity DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<Book> findTopBooksBySoldQuantity(@Param("limit") int limit);
}
