package com.bookstore.Service;

import com.bookstore.DTO.*;
import com.bookstore.Entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    ResponseEntity<GenericResponse> register(Register registerRequest);

    ResponseEntity<GenericResponse> getProfile(String userId);

    <S extends User> S save(S entity);

    ResponseEntity<GenericResponse> validateVerificationAccount(Req_Verify_OTPRegister register);

    public String getUserName(String email);

    public ResponseEntity<GenericResponse> verifyAdmin(Admin_Req_Verify adminReqVerify);

    public ResponseEntity<GenericResponse> verify(String authorizationHeader);

    public ResponseEntity<GenericResponse> getAll(int page, int size, int isActive, int isVerified, String email);

    public ResponseEntity<GenericResponse> updateUserStatus(String userId, Admin_Req_Update_UserStatus adminUpdateUserDTO);

    public ResponseEntity<GenericResponse> login(Login login);

    public ResponseEntity<GenericResponse>  logout(String authorizationHeader, String refreshToken);

    public ResponseEntity<GenericResponse> changePassword(String userId, Req_Update_Password reqUpdatePassword);

    public ResponseEntity<GenericResponse> changeAvatar(MultipartFile file, String userId);

    public ResponseEntity<GenericResponse> changeProfile(String userId, Req_Update_Profile profile);

    public User findOrCreateUser(String email, String fullName);

    public ResponseEntity<GenericResponse> resetPassword(Req_Reset_Password password);
}
