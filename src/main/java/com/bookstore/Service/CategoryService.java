package com.bookstore.Service;

import com.bookstore.DTO.CreateCategory;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Category;
import org.springframework.http.ResponseEntity;

public interface CategoryService {

    ResponseEntity<GenericResponse> create(CreateCategory createCategory);

    ResponseEntity<GenericResponse> getAll();
}
