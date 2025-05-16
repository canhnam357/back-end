package com.bookstore.Repository;

import com.bookstore.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findAllByUserUserId (String userId);

    Optional<Address> findByAddressIdAndUserUserId (String addressId, String userId);

    Optional<Address> findByAddressId (String addressId);

    @Query("SELECT a FROM Address a WHERE a.user.userId = :userId AND a.defaultAddress = true")
    Optional<Address> findDefaultAddressOfUser(@Param("userId") String userId);

    long countByUserUserId(String userId);
}
