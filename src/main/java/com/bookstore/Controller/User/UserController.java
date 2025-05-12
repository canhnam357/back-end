package com.bookstore.Controller.User;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Update_Password;
import com.bookstore.DTO.Req_Update_Profile;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER')")
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/profile")
    public ResponseEntity<GenericResponse> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.getProfile(userId);
    }

    @GetMapping("/user-id")
    public ResponseEntity<GenericResponse> getUserId(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                .success(true)
                .message("Get userId successfully!")
                .result(userId)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<GenericResponse> changePassword(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Req_Update_Password reqUpdatePassword) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.changePassword(userId, reqUpdatePassword);
    }

    @PostMapping("/change-avatar")
    public ResponseEntity<GenericResponse> changeAvatar(@RequestParam("image") MultipartFile file, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.changeAvatar(file, userId);
    }

    @PostMapping("/change-profile")
    public ResponseEntity<GenericResponse> changeProfile(@RequestHeader("Authorization") String authorizationHeader, @RequestBody Req_Update_Profile profile) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        return userService.changeProfile(userId, profile);
    }

    @PostMapping("/send-otp-reset-password")
    public ResponseEntity<GenericResponse> sendOTPResetPassword (@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        String userId = jwtTokenProvider.getUserIdFromJwt(token);
        emailVerificationService.sendOTPChangePassword(userId);
        return ResponseEntity.ok().body(GenericResponse.builder()
                .message("Send OTP Successfully!")
                .statusCode(HttpStatus.OK.value())
                .success(true)
                .build());
    }

}
