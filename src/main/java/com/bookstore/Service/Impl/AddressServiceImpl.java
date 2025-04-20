package com.bookstore.Service.Impl;

import com.bookstore.DTO.Req_Create_Address;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_PatchUpdate_Address;
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
    public ResponseEntity<GenericResponse> create(Req_Create_Address createAddress, String userId) {
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

    @Override
    public ResponseEntity<GenericResponse> update(Req_PatchUpdate_Address address, String userId) {
        try {
            Optional<Address> _address = addressRepository.findByAddressIdAndUserUserId(address.getAddressId(), userId);
            if (_address.isEmpty()) {
                return ResponseEntity.status(404).body(
                        GenericResponse.builder()
                                .message("Address Not Found!!!")
                                .result("")
                                .statusCode(HttpStatus.NOT_FOUND.value())
                                .success(false)
                                .build()
                );
            }

            System.out.println("UPDATED ADDRESS");
            System.out.println(address.getPhoneNumber() != null);
            if (address.getFullName() != null) {
                _address.get().setFullName(address.getFullName());
            }

            if (address.getPhoneNumber() != null) {
                _address.get().setPhoneNumber(address.getPhoneNumber());
            }

            if (address.getAddressInformation() != null) {
                _address.get().setAddressInformation(address.getAddressInformation());
            }

            if (address.getOtherDetail() != null) {
                _address.get().setOtherDetail(address.getOtherDetail());
            }

            addressRepository.save(_address.get());

            return ResponseEntity.status(204).body(
                    GenericResponse.builder()
                            .message("Updated Address Successfully!")
                            .result("")
                            .statusCode(HttpStatus.NO_CONTENT.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Updated Address failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
