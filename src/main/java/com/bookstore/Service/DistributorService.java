package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.Admin_Req_Update_Distributor;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface DistributorService {
    ResponseEntity<GenericResponse> create(Admin_Req_Create_Distributor createDistributor);

    ResponseEntity<GenericResponse> getAll(int page, int size);

    ResponseEntity<GenericResponse> search(int page, int size, String keyword);

    ResponseEntity<GenericResponse> update(String distributorId, Admin_Req_Update_Distributor distributor);

    ResponseEntity<GenericResponse> getAllNotPageable(String keyword);
}
