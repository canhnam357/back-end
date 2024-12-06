package com.bookstore.Service;

import com.bookstore.DTO.CreateContributor;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface ContributorService {
    ResponseEntity<GenericResponse> create(CreateContributor createContributor);

    ResponseEntity<GenericResponse> getAll(int page, int size);
}
