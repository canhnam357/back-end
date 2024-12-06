package com.bookstore.Repository;

import com.bookstore.Entity.Author;
import com.bookstore.Entity.Contributor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributorRepository extends JpaRepository<Contributor, String> {
    Page<Contributor> findAll(Pageable pageable);
}
