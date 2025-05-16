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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserDetailService userDetailService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public <S extends RefreshToken> S save(S entity) {
        return refreshTokenRepository.save(entity);
    }

    @Override
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
                                .message("RefreshToken is not present. Please log in again!")
                                .statusCode(HttpStatus.UNAUTHORIZED.value())
                                .build());
                    }
                    UserDetail userDetail = (UserDetail) userDetailService.loadUserByUserId(jwtTokenProvider.getUserIdFromRefreshToken(reqVerify.getToken()));
                    String accessToken = jwtTokenProvider.generateAccessToken(userDetail);
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("accessToken", accessToken);
                    resultMap.put("refreshToken", reqVerify.getToken());
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
                    .message("Unauthorized. Please log in again!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Failed to reset access token, message = " + e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
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
            e.printStackTrace();
        }
    }

    @Override
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
                            .message("Logged out successfully!")
                            .statusCode(HttpStatus.OK.value())
                            .build());
                    return;
                }
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                        .success(false)
                        .message("Failed to log out!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
                return;
            }
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Failed to log out!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());

        }catch(Exception e){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Failed to log out, message = " + e.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }
}
