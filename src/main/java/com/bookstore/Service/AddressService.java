package com.bookstore.Service;

import com.bookstore.DTO.CreateAddress;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface AddressService {
    ResponseEntity<GenericResponse> getAll(int page, int size, String userId);

    ResponseEntity<GenericResponse> create(CreateAddress createAddress, String userId);

    ResponseEntity<GenericResponse> delete(String addressId, String userId);
}