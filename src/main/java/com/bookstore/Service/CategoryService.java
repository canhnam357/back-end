package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Category;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Category;
import org.springframework.http.ResponseEntity;

public interface CategoryService {

    ResponseEntity<GenericResponse> create(Admin_Req_Create_Category createCategory);

    ResponseEntity<GenericResponse> getAll();
}
