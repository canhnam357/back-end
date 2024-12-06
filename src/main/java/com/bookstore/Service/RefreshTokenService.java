package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.RefreshToken;
import org.springframework.http.ResponseEntity;

public interface RefreshTokenService {
    <S extends RefreshToken> S save(S entity);

    ResponseEntity<GenericResponse> refreshAccessToken(String refreshToken);

    void revokeRefreshToken(String userId);

    ResponseEntity<?> logout(String refreshToken);
}
