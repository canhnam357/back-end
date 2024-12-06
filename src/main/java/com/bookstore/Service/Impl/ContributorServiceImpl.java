package com.bookstore.Service.Impl;

import com.bookstore.DTO.CreateContributor;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Author;
import com.bookstore.Entity.Category;
import com.bookstore.Entity.Contributor;
import com.bookstore.Repository.ContributorRepository;
import com.bookstore.Service.ContributorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ContributorServiceImpl implements ContributorService {
    @Autowired
    private ContributorRepository contributorRepository;
    @Override
    public ResponseEntity<GenericResponse> create(CreateContributor createContributor) {
        try {
            Contributor contributor = new Contributor();
            contributor.setContributorName(createContributor.getContributorName());
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Create Contributor successfully!")
                            .result(contributorRepository.save(contributor))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Create Contributor failed!!!")
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
            Page<Contributor> contributors = contributorRepository.findAll(PageRequest.of(page - 1, size));
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Contributor Successfully!")
                            .result(contributors)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Contributor failed!!!")
                            .result("")
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
