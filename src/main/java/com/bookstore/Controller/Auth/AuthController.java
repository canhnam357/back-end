package com.bookstore.Controller.Auth;

import com.bookstore.DTO.*;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.RefreshTokenService;
import com.bookstore.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {


    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/oauth2/authorization/google")
    public ResponseEntity<?> initiateGoogleLogin() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<GenericResponse> login(@Valid @RequestBody Login loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<GenericResponse> registerProcess(@RequestBody @Valid Register registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse> logout(@RequestHeader("Authorization") String authorizationHeader,
                                    @RequestParam("refreshToken") String refreshToken) {
        return userService.logout(authorizationHeader, refreshToken);
    }

    @PostMapping("/refresh-access-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Req_Verify reqVerify) {
        return refreshTokenService.refreshAccessToken(reqVerify);
    }

    @PostMapping(value = "/verify-otp")
    public ResponseEntity<GenericResponse> confirmRegistration(@RequestBody Req_Verify_OTPRegister register){
        return userService.validateVerificationAccount(register);
    }

    @PostMapping("/verify-admin")
    public ResponseEntity<GenericResponse> verifyAdmin(@RequestBody Admin_Req_Verify verifyAdminDTO){
        return userService.verifyAdmin(verifyAdminDTO);
    }


    @PostMapping("/check")
    public ResponseEntity<GenericResponse> verify(@RequestHeader("Authorization") String authorizationHeader){
        return userService.verify(authorizationHeader);
    }

    @PostMapping("/send-otp-reset-password")
    public ResponseEntity<GenericResponse> sendOtp(@RequestParam(defaultValue = "") String email) {
        return emailVerificationService.sendOTPResetPassword(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponse> resetPassword(@RequestBody Req_Reset_Password password) {
        return userService.resetPassword(password);
    }

}
