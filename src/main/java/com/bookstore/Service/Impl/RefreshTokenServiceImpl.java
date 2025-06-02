package com.bookstore.Service.Impl;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Verify;
import com.bookstore.Entity.RefreshToken;
import com.bookstore.Entity.User;
import com.bookstore.Repository.RefreshTokenRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Security.UserDetail;
import com.bookstore.Security.UserDetailService;
import com.bookstore.Service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserDetailService userDetailService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <S extends RefreshToken> S save(S entity) {
        return refreshTokenRepository.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<GenericResponse> refreshAccessToken(Req_Verify reqVerify){
        try{
            String userId = jwtTokenProvider.getUserIdFromRefreshToken(reqVerify.getToken());
            Optional<User> optionalUser = userRepository.findById(userId);
            if(optionalUser.isPresent() && optionalUser.get().isActive() && optionalUser.get().isVerified()){
                Optional<RefreshToken> token = refreshTokenRepository.findByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(userId);
                if(token.isPresent() && jwtTokenProvider.validateToken(token.get().getToken())){
                    if(!token.get().getToken().equals(reqVerify.getToken())){
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                                .success(false)
                                .message("Không tìm thấy RefreshToken. Vui lòng đăng nhập lại!")
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .build());
                    }
                    UserDetail userDetail = (UserDetail) userDetailService.loadUserByUserId(jwtTokenProvider.getUserIdFromRefreshToken(reqVerify.getToken()));
                    String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("accessToken", accessToken);
                    resultMap.put("username", optionalUser.get().getFullName());
                    return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                            .success(true)
                            .result(resultMap)
                            .statusCode(HttpStatus.OK.value())
                            .build());
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Xác thực thất bại. Vui lòng đăng nhập lại!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        } catch (Exception e) {
            log.error("Thất bại khi làm mới AT, lỗi : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeRefreshToken(String userId){
        try{
            Optional<User> optionalUser = userRepository.findById(userId);
            if(optionalUser.isPresent()&&optionalUser.get().isActive()) {
                List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUser_UserIdAndExpiredIsFalseAndRevokedIsFalse(userId);
                if(refreshTokens.isEmpty()){
                    return;
                }
                refreshTokens.forEach(token -> {
                    token.setRevoked(true);
                    token.setExpired(true);
                });
                refreshTokenRepository.saveAll(refreshTokens);
            }
        }catch (Exception e){
            log.error("Thất bại khi xoá RT, lỗi : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshToken){
        try{
            if(jwtTokenProvider.validateToken(refreshToken)){
                Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByTokenAndExpiredIsFalseAndRevokedIsFalse(refreshToken);
                if(optionalRefreshToken.isPresent()){
                    optionalRefreshToken.get().setRevoked(true);
                    optionalRefreshToken.get().setExpired(true);
                    refreshTokenRepository.save(optionalRefreshToken.get());
                    SecurityContextHolder.clearContext();

                    ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                            .success(true)
                            .message("Đăng xuất thành công!")
                            .statusCode(HttpStatus.OK.value())
                            .build());
                    return;
                }
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                        .success(false)
                        .message("Đăng xuất thất bại!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
                return;
            }
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Đăng xuất thất bại!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());

        }catch(Exception e){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Lỗi hệ thống!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }
}
