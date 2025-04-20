package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Author;
import com.bookstore.Entity.Category;
import com.bookstore.Entity.Distributor;
import com.bookstore.Repository.DistributorRepository;
import com.bookstore.Service.DistributorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DistributorServiceImpl implements DistributorService {
    @Autowired
    private DistributorRepository distributorRepository;
    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Distributor createDistributor) {
        try {
            Distributor distributor = new Distributor();
            distributor.setDistributorName(createDistributor.getDistributorName());
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Create Distributor successfully!")
                            .result(distributorRepository.save(distributor))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Create Distributor failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Distributor> contributors = distributorRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Distributor Successfully!")
                            .result(contributors)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Distributor failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
