package com.bookstore.Service.Impl;

import com.bookstore.Constant.Gender;
import com.bookstore.DTO.*;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Security.JwtTokenProvider;
import com.bookstore.Security.UserDetail;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.RefreshTokenService;
import com.bookstore.Service.RoleService;
import com.bookstore.Service.UserService;
import com.bookstore.Specification.UserSpecification;
import com.bookstore.Utils.Normalized;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private Cloudinary cloudinary;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String getUserName(String email) {
        return userRepository.findByEmail(email).get().getFullName();
    }

    @Override
    public ResponseEntity<GenericResponse> verifyAdmin(Admin_Req_Verify adminReqVerify) {
        try {
            String token = adminReqVerify.getAccessToken();
            if (jwtTokenProvider._validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromJwt(token);
                User user = userRepository.findById(userId).get();
                System.err.println("ROLE " + user.getRole().getName());
                if (user.getRole().getName().equals("ADMIN")) {
                    System.err.println("ADMIN");
                    return ResponseEntity.ok(GenericResponse.builder()
                            .success(true)
                            .message("Verify admin Success")
                            .result(user.getFullName())
                            .statusCode(200)
                            .build());
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                        .success(false)
                        .message("User is not ADMIN!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Wrong token!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("User is not ADMIN or wrong token!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> verify(String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            if (jwtTokenProvider._validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromJwt(token);
                User user = userRepository.findById(userId).get();
                System.err.println("ROLE " + user.getRole().getName());
                if (user.getRole().getName().equals("ADMIN") || (user.isVerified() && user.isActive())) {
                    System.err.println("VERIFIED");
                    return ResponseEntity.ok(GenericResponse.builder()
                            .success(true)
                            .message("Verify Success")
                            .result(user.getFullName())
                            .statusCode(200)
                            .build());
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                        .success(false)
                        .message("User is not admin or not active or not verified!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Wrong token!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Wrong token!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size, int isActive, int isVerified, String email) {

        // ban dau la List<User>
        try {
            if (isActive != 0 && isActive != 1) isActive = 2;
            if (isVerified != 0 && isVerified != 1) isVerified = 2;
            System.err.println("isActive " + isActive);
            System.err.println("isVerified " + isVerified);
            String pattern = "";
            for (char c : email.toCharArray()) {
                pattern += "%" + c + "%";
            }
            if (pattern.isEmpty()) {
                pattern = "%%";
            }
            Specification<User> spec = UserSpecification.withFilters(isActive, isVerified, pattern);
            Page<User> userList = userRepository.findAll(spec, PageRequest.of(page - 1, size));
            List<Admin_Res_Get_Users> res = new ArrayList<>();
            for (User user : userList) {
                String gender = "NULL";
                if (user.getGender() != null) gender = user.getGender().name();
                res.add(new Admin_Res_Get_Users(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getDateOfBirth(),
                        gender,
                        user.isActive(),
                        user.isVerified(),
                        user.getRole().getName()
                ));
            }

            Page<Admin_Res_Get_Users> dtoPage = new PageImpl<>(res, userList.getPageable(), userList.getTotalElements());

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All Users Successfully!")
                    .result(dtoPage)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get all user failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> updateUserStatus(String userId, Admin_Req_Update_UserStatus adminUpdateUserDTO) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found user")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (adminUpdateUserDTO.getActive() == null || adminUpdateUserDTO.getVerified() == null) {
                return ResponseEntity.badRequest().body(GenericResponse.builder()
                        .message("isActive and isVerified must not null!!!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .success(false)
                        .build());
            }

            if (adminUpdateUserDTO.getRole() == null || roleService.findByName(adminUpdateUserDTO.getRole()).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found role")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            System.out.println(Boolean.parseBoolean(adminUpdateUserDTO.getActive()));
            System.out.println(Boolean.parseBoolean(adminUpdateUserDTO.getVerified()));
            User _user = user.get();
            _user.setActive(Boolean.parseBoolean(adminUpdateUserDTO.getActive()));
            _user.setVerified(Boolean.parseBoolean(adminUpdateUserDTO.getVerified()));
            _user.setRole(roleService.findByName(adminUpdateUserDTO.getRole()).get());
            userRepository.save(_user);
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Update status user success!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update status user failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> login(Login login) {
        try {
            if (userRepository.findByEmail(login.getEmail()).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Email does not exists!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            User user = userRepository.findByEmail(login.getEmail()).get();
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Your account is not verified!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build());
            }
            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Your account is not active!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build());
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(),
                            login.getPassword()));

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
            tokenMap.put("username", user.getFullName());

            user.setLastLoginAt(new Date());
            userRepository.save(user);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .success(true)
                    .message("Login successfully!")
                    .result(tokenMap)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Login failed!")
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
                        .message("Logout success!")
                        .statusCode(HttpStatus.OK.value())
                        .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                    .success(false)
                    .message("Logout failed!, Please login before logout!")
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Logout failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> register(Register registerRequest) {
        try {
            if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Password must be between 8 and 32 characters long")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            Optional<User> user = userRepository.findByEmailAndIsVerifiedIsTrue(registerRequest.getEmail());
            if (user.isPresent()) {
                return ResponseEntity.status(409).body(GenericResponse.builder()
                        .success(true)
                        .message("Email already in used!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }

            Optional<User> userNotVerified = userRepository.findByEmailAndIsVerifiedIsFalse(registerRequest.getEmail());

            if (userNotVerified.isPresent()) {
                userRepository.delete(userNotVerified.get());
            }

            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(true)
                        .message("Password and confirm password do not match")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }

            User new_user = new User();
            new_user.setFullName(registerRequest.getFullName());
            new_user.setEmail(registerRequest.getEmail());
            new_user.setUserId(UUID.randomUUID().toString().split("-")[0]);
            new_user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            new_user.setPhoneNumber(registerRequest.getPhoneNumber());
            new_user.setRole(roleService.findByName("USER").get());

            Cart cart = new Cart();

            new_user.setCart(cart);

            userRepository.save(new_user);


            emailVerificationService.sendOtp(registerRequest.getEmail());

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Register Success , please check mail and verify OTP!")
                    .statusCode(200)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message(ex.getMessage())
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getProfile(String userId) {
        try {
            Optional<User> user = userRepository.findByUserIdAndIsActiveIsTrue(userId);
            if (!user.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .message("User is not active!!!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .success(false)
                        .build());
            }
            Profile profile = new Profile();
            profile.setEmail(user.get().getEmail());
            profile.setPhoneNumber(user.get().getPhoneNumber());
            profile.setFullName(user.get().getFullName());
            profile.setAvatar(user.get().getAvatar());
            profile.setGender(user.get().getGender().name());
            profile.setDateOfBirth(user.get().getDateOfBirth());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get Profile User Successfully!")
                    .result(profile)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get Profile User failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> validateVerificationAccount(Req_Verify_OTPRegister register) {
        try {
            String email = register.getEmail();
            String otp = register.getOtp();
            if (email == null) {
                email = "";
            }
            if (otp == null) {
                otp = "";
            }
            System.err.println("EMAIL : " + email);
            System.err.println("OTP : " + otp);
            Optional<EmailVerification> emailVerification = emailVerificationRepository.findByOtpAndEmail(otp, email);
            if (!emailVerification.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                        .message("Invalid token, please check the token again!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .success(false)
                        .build());
            }
            emailVerificationRepository.delete(emailVerification.get());
            User user = userRepository.findByEmail(emailVerification.get().getEmail()).get();
            user.setVerified(true);
            user.setActive(true);
            userRepository.save(user);
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Account verification successful, please login!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Account verification failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }

    public ResponseEntity<GenericResponse> changePassword(String userId, Req_Update_Password reqUpdatePassword) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("User not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

            if (reqUpdatePassword.getNewPassword().length() < 8 || reqUpdatePassword.getNewPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Password must be between 8 and 32 characters long")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }



            User user = userOptional.get();

            if (!passwordEncoder.matches(reqUpdatePassword.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GenericResponse.builder()
                        .success(false)
                        .message("Current password is incorrect!")
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .build());
            }

            if (reqUpdatePassword.getPassword().equals(reqUpdatePassword.getNewPassword())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .success(false)
                        .message("Current password and new password are the same!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .build());
            }

            Optional<EmailVerification> emailVerification = emailVerificationRepository.findByOtpAndEmail(reqUpdatePassword.getOtp(), user.getEmail());

            if (emailVerification.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.builder()
                        .success(false)
                        .message("Wrong OTP!")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
            }

            emailVerificationRepository.delete(emailVerification.get());

            user.setPassword(passwordEncoder.encode(reqUpdatePassword.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Password changed successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Change password failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> changeAvatar(MultipartFile file, String userId) {
        try {
            Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
            String url = (String) data.get("url");
            Optional<User> user = userRepository.findById(userId);
            System.err.println(url);
            user.get().setAvatar(url);
            userRepository.save(user.get());
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Change avatar Successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(url)
                    .success(true)
                    .build());
        } catch (IOException io) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Change avatar failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> changeProfile(String userId, Req_Update_Profile profile) {
        try {
            if (userRepository.findById(userId).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found user!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            User user = userRepository.findById(userId).get();
            user.setFullName(profile.getFullName());
            user.setPhoneNumber(profile.getPhoneNumber());
            user.setGender(Gender.valueOf(profile.getGender()));
            user.setDateOfBirth(profile.getDateOfBirth());
            user = userRepository.save(user);
            Profile res = new Profile();
            res.setFullName(user.getFullName());
            res.setPhoneNumber(user.getPhoneNumber());
            res.setEmail(user.getEmail());
            res.setAvatar(user.getAvatar());
            res.setGender(user.getGender().toString());
            res.setDateOfBirth(user.getDateOfBirth());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Change profile successfully!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Change profile failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public User findOrCreateUser(String email, String fullName) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) return user.get();
        User new_user = new User();
        new_user.setFullName(fullName);
        new_user.setEmail(email);
        new_user.setUserId(UUID.randomUUID().toString().split("-")[0]);
        new_user.setRole(roleService.findByName("USER").get());
        new_user.setVerified(true);
        new_user.setActive(true);
        new_user.setLastLoginAt(new Date());
        Cart cart = new Cart();
        cart = cartRepository.save(cart);

        new_user.setCart(cart);

        new_user = userRepository.save(new_user);

        return new_user;
    }

    @Override
    public ResponseEntity<GenericResponse> resetPassword(Req_Reset_Password password) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(password.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .success(false)
                        .message("User not found!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
            }

            if (password.getNewPassword().length() < 8 || password.getNewPassword().length() > 32) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("Password must be between 8 and 32 characters long")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }


            if (!password.getNewPassword().equals(password.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(GenericResponse.builder()
                        .success(false)
                        .message("New Password and Confirm New Password must same.")
                        .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .build());
            }


            User user = userOptional.get();

            Optional<EmailVerification> emailVerification = emailVerificationRepository.findByOtpAndEmail(password.getOtp(), user.getEmail());

            if (emailVerification.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(GenericResponse.builder()
                        .success(false)
                        .message("Wrong OTP!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
            }

            emailVerificationRepository.delete(emailVerification.get());

            user.setPassword(passwordEncoder.encode(password.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok(GenericResponse.builder()
                    .success(true)
                    .message("Password reset successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GenericResponse.builder()
                    .success(false)
                    .message("Reset password failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build());
        }
    }

}
