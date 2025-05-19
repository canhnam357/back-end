package com.bookstore.Repository;

import com.bookstore.Entity.Book;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {
    List<Book> findAllByDeletedIsFalseAndNewArrivalIsTrue();
    @NotNull Page<Book> findAll(@NotNull Pageable pageable);

    Optional<Book> findByBookIdAndDeletedIsFalse(String bookId);

    Page<Book> findAllByAuthorAuthorId(Pageable pageable, String authorId);

    Page<Book> findAllByPublisherPublisherId(Pageable pageable, String publisherId);

    Page<Book> findAllByDistributorDistributorId(Pageable pageable, String distributorId);

    Page<Book> findAllByCategoriesCategoryId(Pageable pageable, String categoryId);

    @Query("SELECT DISTINCT b.price FROM Book b WHERE b.deleted = false ORDER BY b.price ASC")
    List<BigDecimal> findAllDistinctPricesOrderByAsc();

    @Query("SELECT a FROM Book a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords)) ORDER BY a.createdAt DESC")
    Page<Book> findByNameContainingSubsequence(Pageable pageable, String keywords);

    @Query(value = "SELECT * FROM book WHERE (:keywords IS NULL OR LOWER(name_normalized) LIKE LOWER(:keywords)) AND deleted IS FALSE ORDER BY sold_quantity DESC LIMIT :lim",
            nativeQuery = true)
    List<Book> search(@Param("keywords") String keywords, @Param("lim") int lim);

    @Query("SELECT b FROM Book b " +
            "WHERE b.deleted = false " +
            "AND b.discount IS NOT NULL " +
            "AND b.discount.startDate <= :now " +
            "AND b.discount.endDate >= :now")
    List<Book> findBooksWithActiveDiscount(ZonedDateTime now);

    @Query(value = """
        SELECT b.* FROM book b
        LEFT JOIN review r ON b.book_id = r.book_id
        WHERE b.deleted = false
        GROUP BY b.book_id
        ORDER BY COALESCE(AVG(r.rating), 0) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Book> findTopBooksByAverageRating(@Param("limit") int limit);

    @Query(value = """
        SELECT * FROM book
        WHERE deleted = false
        ORDER BY sold_quantity DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Book> findTopBooksBySoldQuantity(@Param("limit") int limit);

    @Modifying
    @Query("UPDATE Book b SET b.inStock = b.inStock + :quantity WHERE b.bookId = :bookId")
    int updateInStockAdd(@Param("bookId") String bookId, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Book b SET b.soldQuantity = b.soldQuantity + :quantity WHERE b.bookId = :bookId")
    int updateSoldQuantity(@Param("bookId") String bookId, @Param("quantity") int quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.bookId = :bookId")
    Optional<Book> findByIdWithLock(@Param("bookId") String bookId);
}
