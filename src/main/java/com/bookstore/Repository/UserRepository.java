package com.bookstore.Repository;

import com.bookstore.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailAndIsActiveIsTrue(String email);

    Optional<User> findByUserIdAndIsActiveIsTrue(String user_id);

    Optional<User> findByEmail(String email);
}
