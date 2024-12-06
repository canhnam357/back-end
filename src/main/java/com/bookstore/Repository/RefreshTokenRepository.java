package com.bookstore.Repository;

import com.bookstore.Entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    List<RefreshToken> findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(String userId);
    Optional<RefreshToken> findByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(String userId);

    Optional<RefreshToken> findByTokenAndExpiredIsFalseAndRevokedIsFalse(String refreshToken);
}
