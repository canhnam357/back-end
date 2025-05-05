package com.bookstore.Service.Impl;

import com.bookstore.DTO.*;
import com.bookstore.Entity.Address;
import com.bookstore.Repository.AddressRepository;
import com.bookstore.Repository.UserRepository;
import com.bookstore.Service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<GenericResponse> getAll(String userId) {
        try {
            List<Address> addresses = addressRepository.findAllByUserUserId(userId);
            List<Res_Get_Address> res = new ArrayList<>();
            for (Address address : addresses) {
                res.add(new Res_Get_Address(
                        address.getAddressId(),
                        address.getFullName(),
                        address.getPhoneNumber(),
                        address.getAddressInformation(),
                        address.getOtherDetail(),
                        address.getIsDefault()
                ));
            }
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All Address Successfully!")
                    .result(res)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Address failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> create(Req_Create_Address createAddress, String userId) {
        try {
            Address tempAddress = new Address();
            tempAddress.setFullName(createAddress.getFullName());
            tempAddress.setPhoneNumber(createAddress.getPhoneNumber());
            tempAddress.setAddressInformation(createAddress.getAddressInformation());
            tempAddress.setOtherDetail(createAddress.getOtherDetail());
            tempAddress.setUser(userRepository.findById(userId).get());
            if (addressRepository.countByUserUserId(userId) == 0) {
                tempAddress.setIsDefault(true);
            }
            Address address = addressRepository.save(tempAddress);
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create Address successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(new Res_Get_Address(
                            address.getAddressId(),
                            address.getFullName(),
                            address.getPhoneNumber(),
                            address.getAddressInformation(),
                            address.getOtherDetail(),
                            address.getIsDefault()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create Address failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String addressId, String userId) {
        try {
            Optional<Address> address = addressRepository.findByAddressIdAndUserUserId(addressId, userId);
            if (!address.isPresent()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Address Not Found!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }

            if (address.get().getIsDefault()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                        .message("Can't delete default address!!!")
                        .statusCode(HttpStatus.CONFLICT.value())
                        .success(false)
                        .build());
            }

            addressRepository.delete(address.get());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                    .message("Delete Address Successfully!")
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                .message("Delete Address failed!!!")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .success(false)
                .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(Req_PatchUpdate_Address tempAddress, String userId, String addressId) {
        try {
            Optional<Address> _address = addressRepository.findByAddressIdAndUserUserId(addressId, userId);
            if (_address.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Address Not Found!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (tempAddress.getFullName() != null) {
                _address.get().setFullName(tempAddress.getFullName());
            }

            if (tempAddress.getPhoneNumber() != null) {
                _address.get().setPhoneNumber(tempAddress.getPhoneNumber());
            }

            if (tempAddress.getAddressInformation() != null) {
                _address.get().setAddressInformation(tempAddress.getAddressInformation());
            }

            if (tempAddress.getOtherDetail() != null) {
                _address.get().setOtherDetail(tempAddress.getOtherDetail());
            }

            Address address = addressRepository.save(_address.get());
            return ResponseEntity.status(HttpStatus.OK).body(GenericResponse.builder()
                    .message("Update Address successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(new Res_Get_Address(
                            address.getAddressId(),
                            address.getFullName(),
                            address.getPhoneNumber(),
                            address.getAddressInformation(),
                            address.getOtherDetail(),
                            address.getIsDefault()
                    ))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Updated Address failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> setDefault(String userId, String addressId) {
        try {
            if (addressRepository.findByAddressIdAndUserUserId(addressId, userId).isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found Address!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            if (addressRepository.findDefaultAddressOfUser(userId).isPresent()) {
                System.err.println(addressRepository.findDefaultAddressOfUser(userId).get().getAddressId());
                System.err.println(addressId);
                if (addressRepository.findDefaultAddressOfUser(userId).get().getAddressId().equals(addressId)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(GenericResponse.builder()
                            .message("Address already is default!!!")
                            .statusCode(HttpStatus.CONFLICT.value())
                            .success(false)
                            .build());
                }
                Address address = addressRepository.findDefaultAddressOfUser(userId).get();
                address.setIsDefault(false);
                addressRepository.save(address);
            }
            Address address = addressRepository.findByAddressId(addressId).get();
            address.setIsDefault(true);
            addressRepository.save(address);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(GenericResponse.builder()
                    .message("Set Address default success!!!")
                    .statusCode(HttpStatus.NO_CONTENT.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Set Address default failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
