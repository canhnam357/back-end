package com.bookstore.Service;

import com.bookstore.DTO.Login;
import org.springframework.http.ResponseEntity;
import com.bookstore.DTO.GenericResponse;

public interface AuthService {
    ResponseEntity<GenericResponse> login(Login login);
    ResponseEntity<GenericResponse> logout(String authorizationHeader, String refreshToken);
}