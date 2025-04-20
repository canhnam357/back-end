package com.bookstore.Repository;

import com.bookstore.Entity.Distributor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributorRepository extends JpaRepository<Distributor, String> {
    Page<Distributor> findAll(Pageable pageable);
}
