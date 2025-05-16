package com.bookstore.Service;

import com.bookstore.DTO.*;
import com.bookstore.Entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    ResponseEntity<GenericResponse> register(Register registerRequest);

    ResponseEntity<GenericResponse> getProfile(String userId);

    <S extends User> S save(S entity);

    ResponseEntity<GenericResponse> validateVerificationAccount(Req_Verify_OTPRegister register);

    ResponseEntity<GenericResponse> verifyAdmin(Admin_Req_Verify adminReqVerify);

    ResponseEntity<GenericResponse> verify(String authorizationHeader);

    ResponseEntity<GenericResponse> getAll(int page, int size, int isActive, int isVerified, String email);

    ResponseEntity<GenericResponse> updateUserStatus(String userId, Admin_Req_Update_UserStatus adminUpdateUserDTO);

    ResponseEntity<GenericResponse> changePassword(String userId, Req_Update_Password reqUpdatePassword);

    ResponseEntity<GenericResponse> changeAvatar(MultipartFile file, String userId);

    ResponseEntity<GenericResponse> changeProfile(String userId, Req_Update_Profile profile);

    User findOrCreateUser(String email, String fullName);

    ResponseEntity<GenericResponse> resetPassword(Req_Reset_Password password);

    ResponseEntity<GenericResponse> getUserById(String userId);

    ResponseEntity<GenericResponse> countVerifiedUsersByMonth(int year);
}
