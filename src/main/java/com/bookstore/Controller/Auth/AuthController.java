package com.bookstore.Controller.Auth;

import com.bookstore.DTO.*;
import com.bookstore.Entity.RefreshToken;
import com.bookstore.Entity.User;
import com.bookstore.Exception.UserNotFoundException;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Security.UserDetail;
import com.bookstore.Security.UserDetailService;
import com.bookstore.Service.RefreshTokenService;
import com.bookstore.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;



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
    public ResponseEntity<GenericResponse> confirmRegistration(@RequestParam(defaultValue = "") String email,
                                                               @RequestParam(defaultValue = "") String otp){
        return userService.validateVerificationAccount(email, otp);
    }

    @PostMapping("/verify-admin")
    public ResponseEntity<GenericResponse> verifyAdmin(@RequestBody Admin_Req_Verify verifyAdminDTO){
        return userService.verifyAdmin(verifyAdminDTO);
    }


    @PostMapping("/verify")
    public ResponseEntity<GenericResponse> verify(@RequestBody Req_Verify reqVerify){
        return userService.verify(reqVerify);
    }

}
