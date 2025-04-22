package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Distributor;
import com.bookstore.DTO.Admin_Req_Update_Distributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Author;
import com.bookstore.Entity.Category;
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

@Service
public class DistributorServiceImpl implements DistributorService {
    @Autowired
    private DistributorRepository distributorRepository;
    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Distributor createDistributor) {
        try {
            Distributor distributor = new Distributor();
            distributor.setDistributorName(createDistributor.getDistributorName());
            distributor.setNameNormalized(Normalized.removeVietnameseAccents(createDistributor.getDistributorName()));
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

    @Override
    public ResponseEntity<GenericResponse> search(int page, int size, String keyword) {
        try {
            String s = Normalized.removeVietnameseAccents(keyword);
            String search_word = "";
            for (char c : s.toCharArray()) {
                search_word += "%" + c + "%";
            }

            if (search_word.length() == 0) {
                search_word = "%%";
            }

            System.err.println(search_word);

            Page<Distributor> distributors = distributorRepository.findByNameContainingSubsequence(PageRequest.of(page - 1, size), search_word);

            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Distributor Successfully!")
                            .result(distributors)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Distributor failed!")
                            .result("")
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String distributorId, Admin_Req_Update_Distributor distributor) {
        try {
            Distributor _distributor = distributorRepository.findById(distributorId).get();
            _distributor.setDistributorName(distributor.getDistributorName());
            _distributor.setNameNormalized(Normalized.removeVietnameseAccents(distributor.getDistributorName()));
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Update Distributor successfully!")
                            .result(distributorRepository.save(_distributor))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Update Distributor failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAllNotPageable(String keyword) {
        try {
            String s = Normalized.removeVietnameseAccents(keyword);
            String search_word = "";
            for (char c : s.toCharArray()) {
                search_word += "%" + c + "%";
            }

            if (search_word.length() == 0) {
                search_word = "%%";
            }

            System.err.println(search_word);

            List<Distributor> distributors = distributorRepository.findListByNameContainingSubsequence(search_word);

            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Search Distributor Successfully!")
                            .result(distributors)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Search Distributor failed!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
