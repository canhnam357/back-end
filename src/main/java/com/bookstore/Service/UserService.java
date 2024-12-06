package com.bookstore.Service;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.RegisterRequest;
import com.bookstore.Entity.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    ResponseEntity<GenericResponse> customerRegister(RegisterRequest registerRequest);

    ResponseEntity<GenericResponse> getProfile(String userId);

    <S extends User> S save(S entity);
}
