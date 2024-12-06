package com.bookstore.Service.Impl;

import com.bookstore.DTO.CreateAddress;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Address;
import com.bookstore.Entity.Author;
import com.bookstore.Repository.AddressRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size, String userId) {
        try {
            Page<Address> addresses = addressRepository.findAllByUserUserId(userId, PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Address Successfully!")
                            .result(addresses)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Address failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> create(CreateAddress createAddress, String userId) {
        try {
            Address address = new Address();
            address.setFullName(createAddress.getFullName());
            address.setPhoneNumber(createAddress.getPhoneNumber());
            address.setAddressInformation(createAddress.getAddressInformation());
            address.setOtherDetail(createAddress.getOtherDetail());
            address.setUser(userRepository.findById(userId).get());
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Create Address successfully!")
                            .result(addressRepository.save(address))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Create Address failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String addressId, String userId) {
        try {
            Optional<Address> address = addressRepository.findByAddressIdAndUserUserId(addressId, userId);
            if (!address.isPresent()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Address Not Found!!!")
                                .result("")
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }
            addressRepository.delete(address.get());
            return ResponseEntity.status(204).body(
                    GenericResponse.builder()
                            .message("Delete Address Successfully!")
                            .result("")
                            .statusCode(HttpStatus.NO_CONTENT.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Delete Address failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
