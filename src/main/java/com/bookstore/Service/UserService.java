package com.bookstore.Service;

import com.bookstore.DTO.*;
import com.bookstore.Entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    ResponseEntity<GenericResponse> register(Register registerRequest);

    ResponseEntity<GenericResponse> getProfile(String userId);

    <S extends User> S save(S entity);

    ResponseEntity<GenericResponse> validateVerificationAccount(String email, String otp);

    public String getUserName(String email);

    public ResponseEntity<GenericResponse> verifyAdmin(Admin_Req_Verify adminReqVerify);

    public ResponseEntity<GenericResponse> verify(Req_Verify reqVerify);

    public ResponseEntity<GenericResponse> getAll(int page, int size, int isActive, int isVerified, String email);

    public ResponseEntity<GenericResponse> updateUserStatus(String userId, Admin_Req_Update_UserStatus adminUpdateUserDTO);

    public ResponseEntity<GenericResponse> login(Login login);

    public ResponseEntity<GenericResponse>  logout(String authorizationHeader, String refreshToken);
}
