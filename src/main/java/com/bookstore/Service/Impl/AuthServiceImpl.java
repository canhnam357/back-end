// com.bookstore.Service.Impl.AuthServiceImpl
package com.bookstore.Service.Impl;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Login;
import com.bookstore.Entity.RefreshToken;
import com.bookstore.Entity.User;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Security.UserDetail;
import com.bookstore.Service.AuthService;
import com.bookstore.Service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public ResponseEntity<GenericResponse> login(Login login) {
        try {
            if (login.getEmail() == null || login.getEmail().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Email must be provided!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (login.getEmail().length() > 300) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Email length must be less than or equal to 300!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (login.getPassword() == null || login.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Password must be provided!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (login.getPassword().length() < 8 || login.getPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Password length must be between 8 and 32 characters!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            Optional<User> userOptional = userRepository.findByEmail(login.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Account does not exist!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            User user = userOptional.get();
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Account is not verified!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build());
            }
            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Account is not active!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build());
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetail userDetail = (UserDetail) authentication.getPrincipal();
            String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
            RefreshToken refreshToken = new RefreshToken();
            String token = jwtTokenProvider.generateRefreshToken(userDetail);
            refreshToken.setToken(token);
            refreshToken.setUser(userDetail.getUser());
            refreshTokenService.revokeRefreshToken(userDetail.getUserId());
            refreshTokenService.save(refreshToken);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", accessToken);
            tokenMap.put("refreshToken", token);
            tokenMap.put("username", user.getFullName());

            user.setLastLoginAt(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            userRepository.save(user);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Logged in successfully!")
                    .result(tokenMap)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                    .success(false)
                    .message("Incorrect password!")
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build());
        } catch (Exception ex) {
            log.error("Đăng nhập thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Failed to log in, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> logout(String authorizationHeader, String refreshToken) {
        try {
            String accessToken = authorizationHeader.substring(7);
            if (jwtTokenProvider.getUserIdFromJwt(accessToken).equals(jwtTokenProvider.getUserIdFromRefreshToken(refreshToken))) {
                refreshTokenService.logout(refreshToken);
                return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                        .success(true)
                        .message("Logged out successfully!")
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Failed to log out, please log in before logging out!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        } catch (Exception ex) {
            log.error("Đăng xuất thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Failed to log out, message = " + ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }
}