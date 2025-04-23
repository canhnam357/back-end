package com.bookstore.Controller.User;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_Update_Password;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE', 'USER')")
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
                .message("Get userId success")
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
}
