package com.bookstore.Repository;

import com.bookstore.Entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndVerifiedIsTrue(String email);

    Optional<User> findByEmailAndVerifiedIsFalse(String email);

    Optional<User> findByUserIdAndActiveIsTrue(String user_id);

    Optional<User> findByEmail(String email);

    @NotNull Page<User> findAll(@NotNull Pageable pageable);

    @Query(value = "SELECT MONTH(created_at) AS month, COUNT(*) " +
            "FROM users " +
            "WHERE verified = true AND YEAR(created_at) = :year " +
            "GROUP BY MONTH(created_at)", nativeQuery = true)
    List<Object[]> countVerifiedUsersByMonth(@Param("year") int year);

}
