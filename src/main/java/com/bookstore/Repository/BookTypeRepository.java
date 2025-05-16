package com.bookstore.Repository;

import com.bookstore.Entity.BookType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTypeRepository extends JpaRepository<BookType, String> {
    @NotNull Page<BookType> findAll(@NotNull Pageable pageable);
}
