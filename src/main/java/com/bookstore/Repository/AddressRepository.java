package com.bookstore.Repository;

import com.bookstore.Entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {

    Page<Address> findAllByUserUserId (String userId, Pageable pageable);

    Optional<Address> findByAddressIdAndUserUserId (String addressId, String userId);
}
