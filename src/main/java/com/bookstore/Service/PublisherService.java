package com.bookstore.Service;

import com.bookstore.DTO.CreatePublisher;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface PublisherService {
    ResponseEntity<GenericResponse> create(CreatePublisher createPublisher);

    ResponseEntity<GenericResponse> getAll(int page, int size);
}
