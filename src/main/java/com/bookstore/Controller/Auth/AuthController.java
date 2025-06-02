package com.bookstore.Controller.Auth;

import com.bookstore.DTO.*;
import com.bookstore.Service.AuthService;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.RefreshTokenService;
import com.bookstore.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AuthController {


    private final UserService userService;

    private final RefreshTokenService refreshTokenService;

    private final EmailVerificationService emailVerificationService;

    private final AuthService authService;

    @GetMapping("/oauth2/authorization/google")
    public ResponseEntity<?> initiateGoogleLogin() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<GenericResponse> login(@Valid @RequestBody Login loginDTO) {
        return authService.login(loginDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<GenericResponse> registerProcess(@RequestBody @Valid Register registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse> logout(@RequestHeader("Authorization") String authorizationHeader,
                                                  @CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        log.info("refreshToken : " + refreshToken);
        return authService.logout(authorizationHeader, refreshToken);
    }

    @PostMapping("/refresh-access-token")
    public ResponseEntity<?> refreshAccessToken(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        log.info("refreshToken : " + refreshToken);
        return refreshTokenService.refreshAccessToken(new Req_Verify(refreshToken));
    }

    @PostMapping(value = "/verify-otp")
    public ResponseEntity<GenericResponse> confirmRegistration(@RequestBody Req_Verify_OTPRegister register){
        return userService.validateVerificationAccount(register);
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
