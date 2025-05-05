package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.Admin_Req_Update_Distributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Distributor;
import com.bookstore.Repository.DistributorRepository;
import com.bookstore.Service.DistributorService;
import com.bookstore.Utils.Normalized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DistributorServiceImpl implements DistributorService {
    @Autowired
    private DistributorRepository distributorRepository;
    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Distributor createDistributor) {
        try {
            Distributor distributor = new Distributor();
            distributor.setDistributorName(createDistributor.getDistributorName());
            distributor.setNameNormalized(Normalized.remove(createDistributor.getDistributorName()));
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create Distributor successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .result(distributorRepository.save(distributor))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create Distributor failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll(int page, int size) {
        try {
            Page<Distributor> contributors = distributorRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All Distributor Successfully!")
                    .result(contributors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Distributor failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> search(int page, int size, String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            Page<Distributor> distributors = distributorRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Search Distributor Successfully!")
                    .result(distributors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Search Distributor failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String distributorId, Admin_Req_Update_Distributor distributor) {
        try {
            if (distributorRepository.findById(distributorId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GenericResponse.builder()
                        .message("Not found distributor!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            Distributor _distributor = distributorRepository.findById(distributorId).get();
            _distributor.setDistributorName(distributor.getDistributorName());
            _distributor.setNameNormalized(Normalized.remove(distributor.getDistributorName()));
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Update Distributor successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .result(distributorRepository.save(_distributor))
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update Distributor failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllNotPageable(String keyword) {
        try {
            String search_word = Normalized.removeVietnameseAccents(keyword);

            List<Distributor> distributors = distributorRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Search Distributor Successfully!")
                    .result(distributors)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Search Distributor failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String distributorId) {
        try {
            Optional<Distributor> distributor = distributorRepository.findById(distributorId);
            if (distributor.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found Distributor!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            distributorRepository.delete(distributor.get());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Delete distributor successfully!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Delete distributor failed!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
