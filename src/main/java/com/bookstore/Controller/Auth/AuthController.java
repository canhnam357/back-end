package com.bookstore.Controller.Auth;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Login;
import com.bookstore.DTO.RegisterRequest;
import com.bookstore.DTO.VerifyDTO;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TemplateEngine templateEngine;


    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody Login loginDTO) {

        if (userService.findByEmail(loginDTO.getEmail()).isEmpty())
            throw new UserNotFoundException("Account does not exist");
        Optional<User> optionalUser = userService.findByEmail(loginDTO.getEmail());
        if (optionalUser.isPresent() && !optionalUser.get().isVerified()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Your account is not verified!")
                    .result(null)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),
                        loginDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetail userDetail = (UserDetail) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
        RefreshToken refreshToken = new RefreshToken();
        String token = jwtTokenProvider.generateRefreshToken(userDetail);
        refreshToken.setToken(token);
        refreshToken.setUser(userDetail.getUser());
        //invalid all refreshToken before
        refreshTokenService.revokeRefreshToken(userDetail.getUserId());
        refreshTokenService.save(refreshToken);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", token);

        if (optionalUser.isPresent()) {
            optionalUser.get().setLastLoginAt(new Date());
            userService.save(optionalUser.get());
        }
        return ResponseEntity.ok().body(GenericResponse.builder()
                .success(true)
                .message("Login successfully!")
                .result(tokenMap)
                .statusCode(HttpStatus.OK.value())
                .build());
    }


    @PostMapping("/register")
    public ResponseEntity<GenericResponse> registerProcess(
            @RequestBody @Valid RegisterRequest registerRequest,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(
                    bindingResult.getFieldError()).getDefaultMessage();

            return ResponseEntity.status(500)
                    .body(new GenericResponse(
                            false,
                            errorMessage,
                            null,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
        return userService.customerRegister(registerRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader,
                                    @RequestParam("refreshToken") String refreshToken) {
        String accessToken = authorizationHeader.substring(7);
        if (jwtTokenProvider.getUserIdFromJwt(accessToken).equals(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken))) {
            return refreshTokenService.logout(refreshToken);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Logout failed!")
                        .result("Please login before logout!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestHeader("Authorization") String authorizationHeader,
                                       @RequestParam("refreshToken") String refreshToken) {
        String accessToken = authorizationHeader.substring(7);
        if (jwtTokenProvider.getUserIdFromJwt(accessToken).equals(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken))) {
            String userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
            refreshTokenService.revokeRefreshToken(userId);
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Logout successfully!")
                    .result("")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("Logout failed!")
                        .result("Please login before logout!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
    }

//    @PostMapping("/refresh-access-token")
//    public ResponseEntity<?> refreshAccessToken(@RequestBody TokenRequest tokenRequest) {
//        String refreshToken = tokenRequest.getRefreshToken();
//        return refreshTokenService.refreshAccessToken(refreshToken);
//    }

    @PostMapping(value = "/verify")
    public ResponseEntity<GenericResponse> confirmRegistration(@RequestBody VerifyDTO verifyDTO){
        return userService.validateVerificationAccount(verifyDTO.getOtp());
    }

}
