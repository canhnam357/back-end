package com.bookstore.Service;

import com.bookstore.DTO.CreateAuthor;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface AuthorService {
    ResponseEntity<GenericResponse> create(CreateAuthor createAuthor);

    ResponseEntity<GenericResponse> getAll(int page, int size);
}
