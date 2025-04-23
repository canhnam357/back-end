package com.bookstore.Controller.General;

import com.bookstore.DTO.Req_Verify_Email;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class EmailVerificationController {
    @Autowired
    private EmailVerificationService emailVerificationService;

    @PostMapping("/sendOTP")
    public ResponseEntity<GenericResponse> sendOtp(@RequestBody Req_Verify_Email emailVerificationRequest) {
        try {
            emailVerificationService.sendOtp(emailVerificationRequest.getEmail());
            return ResponseEntity.ok()
                    .body(GenericResponse.builder()
                            .success(true)
                            .message("OTP sent successfully!")
                            .statusCode(HttpStatus.OK.value())
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(GenericResponse.builder()
                            .success(false)
                            .message("An error occurred while sending OTP.")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .build());
        }
    }
}

