package com.bookstore.Service.Impl;

import com.bookstore.DTO.*;
import com.bookstore.Entity.*;
import com.bookstore.Repository.*;
import com.bookstore.Service.EmailVerificationService;
import com.bookstore.Service.RoleService;
import com.bookstore.Specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements com.bookstore.Service.UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RoleService roleService;
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String getUserName(String email) {
        return userRepository.findByEmail(email).get().getFullName();
    }

    @Override
    public ResponseEntity<GenericResponse> verifyAdmin(String userId) {
        User user = userRepository.findById(userId).get();
        System.err.println("ROLE " + user.getRole().getName());
        if (user.getRole().getName().equals("ADMIN")) {
            System.err.println("ADMIN");
            return ResponseEntity.ok(
                    GenericResponse.builder()
                            .success(true)
                            .message("Verify admin Success")
                            .result(user.getFullName())
                            .statusCode(200)
                            .build()
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .success(false)
                        .message("User is not ADMIN!")
                        .result("")
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .build());
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size, int isActive, int isVerified, String email) {

        // ban dau la List<User>
        try {
            if (isActive != 0 && isActive != 1) isActive = 2;
            if (isVerified != 0 && isVerified != 1) isVerified = 2;
            System.err.println("isActive " + isActive);
            System.err.println("isVerified " + isVerified);
            Specification<User> spec = UserSpecification.withFilters(isActive, isVerified, email);
            Page<User> userList = userRepository.findAll(spec, PageRequest.of(page - 1, size));
            List<GetAllUsersDTO> res = new ArrayList<>();
            for (User user : userList) {
                String gender = "NULL";
                if (user.getGender() != null) gender = user.getGender().name();
                res.add(new GetAllUsersDTO(
                        user.getUserId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getDateOfBirth(),
                        gender,
                        user.isActive(),
                        user.isVerified()
                ));
            }

            Page<GetAllUsersDTO> dtoPage = new PageImpl<>(res, userList.getPageable(), userList.getTotalElements());

            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Users Successfully!")
                            .result(dtoPage)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get all user failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> updateUserStatus(String userId, Admin_UpdateUserDTO adminUpdateUserDTO) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Not found user")
                                .result("")
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }
            if (adminUpdateUserDTO.getActive() == null || adminUpdateUserDTO.getVerified() == null) {
                return ResponseEntity.badRequest().body(
                        GenericResponse.builder()
                                .message("isActive and isVerified must not null!!!")
                                .result("")
                                .statusCode(HttpStatus.BAD_REQUEST.value())
                                .success(false)
                                .build()
                );
            }
            System.out.println(Boolean.parseBoolean(adminUpdateUserDTO.getActive()));
            System.out.println(Boolean.parseBoolean(adminUpdateUserDTO.getVerified()));
            User _user = user.get();
            _user.setActive(Boolean.parseBoolean(adminUpdateUserDTO.getActive()));
            _user.setVerified(Boolean.parseBoolean(adminUpdateUserDTO.getVerified()));
            userRepository.save(_user);
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Update status user success!!!")
                            .result("")
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Update status user failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> customerRegister(RegisterRequest registerRequest) {
        if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32)
            throw new RuntimeException("Password must be between 8 and 32 characters long");

        //Optional<User> userOptional = findByEmail(registerRequest.getEmail());
        Optional<User> userOptional = userRepository.findByEmailAndIsVerifiedIsTrue(registerRequest.getEmail());
        if (userOptional.isPresent())
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Email already in used!")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );
        Optional<User> userNotVerified = userRepository.findByEmailAndIsVerifiedIsFalse(registerRequest.getEmail());

        if (userNotVerified.isPresent()) {
            userRepository.delete(userNotVerified.get());
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
            return ResponseEntity.status(409)
                    .body(
                            GenericResponse.builder()
                                    .success(true)
                                    .message("Password and confirm password do not match")
                                    .result(null)
                                    .statusCode(HttpStatus.CONFLICT.value())
                                    .build()
                    );

        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setUserId(UUID.randomUUID().toString().split("-")[0]);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        System.err.println(registerRequest.getPhoneNumber());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(roleService.findByName("USER"));

        Cart cart = new Cart();
        cart = cartRepository.save(cart);

        user.setCart(cart);

        userRepository.save(user);

        emailVerificationService.sendOtp(registerRequest.getEmail());

        return ResponseEntity.ok(
                GenericResponse.builder()
                        .success(true)
                        .message("Sign Up Success")
                        .result(null)
                        .statusCode(200)
                        .build()
        );
    }

    @Override
    public ResponseEntity<GenericResponse> getProfile(String userId) {
        try {
            Optional<User> user = userRepository.findByUserIdAndIsActiveIsTrue(userId);
            if (!user.isPresent()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Get Book Failed!!!")
                                .result("")
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }
            Profile profile = new Profile();
            profile.setEmail(user.get().getEmail());
            profile.setPhoneNumber(user.get().getPhoneNumber());
            profile.setFullName(user.get().getFullName());
            profile.setAddressList(new ArrayList<>());
            for (Address address : user.get().getAddresses()) {
                AddressResponse addressResponse = new AddressResponse();
                addressResponse.setAddressId(address.getAddressId());
                addressResponse.setAddressInformation(address.getAddressInformation());
                addressResponse.setPhoneNumber(address.getPhoneNumber());
                addressResponse.setFullName(address.getFullName());
                profile.addAddress(addressResponse);
            }
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get Profile User Successfully!")
                            .result(profile)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get Profile User failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> validateVerificationAccount(String otp) {
        Optional<EmailVerification> emailVerification = emailVerificationRepository.findByOtp(otp);
        if (!emailVerification.isPresent()) {
            return ResponseEntity.badRequest().body(
                    GenericResponse.builder()
                            .message("Invalid token, please check the token again!")
                            .result("")
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .success(false)
                            .build()
            );
        }
        User user = userRepository.findByEmail(emailVerification.get().getEmail()).get();
        user.setVerified(true);
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok().body(
                GenericResponse.builder()
                        .message("Account verification successful, please login!")
                        .result("")
                        .statusCode(HttpStatus.OK.value())
                        .success(true)
                        .build()
        );
    }

    @Override
    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }
}
