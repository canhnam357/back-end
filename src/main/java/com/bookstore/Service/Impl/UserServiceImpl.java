package com.bookstore.Service.Impl;

import com.bookstore.DTO.AddressResponse;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Profile;
import com.bookstore.DTO.RegisterRequest;
import com.bookstore.Entity.Address;
import com.bookstore.Entity.Cart;
import com.bookstore.Entity.User;
import com.bookstore.Repository.CartRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements com.bookstore.Service.UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RoleService roleService;
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public ResponseEntity<GenericResponse> customerRegister(RegisterRequest registerRequest) {
        if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 32)
            throw new RuntimeException("Password must be between 8 and 32 characters long");

        Optional<User> userOptional = findByEmail(registerRequest.getEmail());
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
        user.setRole(roleService.findByName("USER"));

        Cart cart = new Cart();
        cart = cartRepository.save(cart);

        user.setCart(cart);

        userRepository.save(user);

        //emailVerificationService.sendOtp(registerRequest.getEmail());

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
    public <S extends User> S save(S entity) {
        return userRepository.save(entity);
    }
}
