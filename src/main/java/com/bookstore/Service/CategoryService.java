package com.bookstore.Service;

import com.bookstore.DTO.Admin_Req_Create_Category;
import com.bookstore.DTO.Admin_Req_Update_Category;
import com.bookstore.DTO.GenericResponse;
import org.springframework.http.ResponseEntity;

public interface CategoryService {

    ResponseEntity<GenericResponse> create(Admin_Req_Create_Category createCategory);

    ResponseEntity<GenericResponse> getAll(String keyword);

    ResponseEntity<GenericResponse> update(String categoryId, Admin_Req_Update_Category category);

    ResponseEntity<GenericResponse> getAllNotPageable(String keyword);
}
