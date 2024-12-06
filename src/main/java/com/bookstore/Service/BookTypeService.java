package com.bookstore.Service;

import com.bookstore.DTO.CreateBookType;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface BookTypeService {
    ResponseEntity<GenericResponse> create(CreateBookType createBookType);

    ResponseEntity<GenericResponse> getAll(int page, int size);
}
