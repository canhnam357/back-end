package com.bookstore.Repository;

import com.bookstore.Entity.Distributor;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DistributorRepository extends JpaRepository<Distributor, String> {
    @NotNull Page<Distributor> findAll(@NotNull Pageable pageable);

    @Query("SELECT a FROM Distributor a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    Page<Distributor> findByNameContainingSubsequence(Pageable pageable, String keywords);
    @Query("SELECT a FROM Distributor a WHERE " +
            "(:keywords IS NULL OR " +
            "LOWER(a.nameNormalized) LIKE LOWER(:keywords))")
    List<Distributor> findListByNameContainingSubsequence(String keywords);
}
