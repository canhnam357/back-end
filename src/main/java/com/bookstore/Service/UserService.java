package com.bookstore.Service;

import com.bookstore.DTO.Admin_UpdateUserDTO;
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

    ResponseEntity<GenericResponse> validateVerificationAccount(String token);

    public String getUserName(String email);

    public ResponseEntity<GenericResponse> verifyAdmin(String userId);

    public ResponseEntity<GenericResponse> getAll(int page, int size, int isActive, int isVerified, String email);

    public ResponseEntity<GenericResponse> updateUserStatus(String userId, Admin_UpdateUserDTO adminUpdateUserDTO);
}
