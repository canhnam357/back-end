package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Verify;
import com.bookstore.Entity.RefreshToken;
import org.springframework.http.ResponseEntity;

public interface RefreshTokenService {
    <S extends RefreshToken> S save(S entity);

    ResponseEntity<GenericResponse> refreshAccessToken(Req_Verify reqVerify);

    void revokeRefreshToken(String userId);

    void logout(String refreshToken);
}
