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
import org.springframework.http.ResponseCookie;
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
                        .message("Email không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (login.getEmail().length() > 255) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Độ dài của email tối đa 255 ký tự!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (login.getPassword() == null || login.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Mật khẩu không được để trống!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            if (login.getPassword().length() < 8 || login.getPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Độ dài của mật khẩu phải có từ 8 tới 32 ký tự!")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            Optional<User> userOptional = userRepository.findByEmail(login.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Tài khoản không tồn tại!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            User user = userOptional.get();
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Tài khoản chưa xác thực, vui lòng đăng ký lại!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build());
            }
            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Tài khoản đang bị khoá!")
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
            tokenMap.put("username", user.getFullName());

            user.setLastLoginAt(ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            userRepository.save(user);

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", token)
                    .httpOnly(true)
                    //.secure(true) // dùng HTTPS thì true
                    .path("/") // chỉ gửi kèm khi gọi endpoint refresh
                    .maxAge(7 * 24 * 60 * 60) // 7 ngày
                    .sameSite("Lax") // hoặc "Strict" tùy use case
                    .build();

            return ResponseEntity.status(HttpStatus.OK).header("Set-Cookie", refreshTokenCookie.toString()).body(GenericResponse.builder()
                    .success(true)
                    .message("Đăng nhập thành công!")
                    .result(tokenMap)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                    .success(false)
                    .message("Mật khẩu không chính xác!")
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .build());
        } catch (Exception ex) {
            log.error("Đăng nhập thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống!")
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
                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        //.secure(true) // dùng HTTPS thì true
                        .path("/") // chỉ gửi kèm khi gọi endpoint refresh
                        .maxAge(0)
                        .sameSite("Lax") // hoặc "Strict" tùy use case
                        .build();
                return ResponseEntity.status(HttpStatus.OK).header("Set-Cookie", refreshTokenCookie.toString()).body(GenericResponse.builder()
                        .success(true)
                        .message("Đăng xuất thành công!")
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                    .success(false)
                    .message("Đăng xuất thất bại, vui lòng đăng nhập trước khi đăng xuất!")
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .build());
        } catch (Exception ex) {
            log.error("Đăng xuất thất bại, lỗi : " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }
}