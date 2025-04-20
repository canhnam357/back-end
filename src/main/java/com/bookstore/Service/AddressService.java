package com.bookstore.Service;

import com.bookstore.DTO.Req_Create_Address;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.DTO.Req_PatchUpdate_Address;
import org.springframework.http.ResponseEntity;

public interface AddressService {
    ResponseEntity<GenericResponse> getAll(int page, int size, String userId);

    ResponseEntity<GenericResponse> create(Req_Create_Address createAddress, String userId);

    ResponseEntity<GenericResponse> delete(String addressId, String userId);

    ResponseEntity<GenericResponse> update(Req_PatchUpdate_Address address, String userId);
}
