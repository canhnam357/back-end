package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Category;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Author;
import com.bookstore.Entity.Category;
import com.bookstore.Repository.CategoryRepository;
import com.bookstore.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;


    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Category createCategory) {
        try {
            Category category = new Category();
            category.setCategoryName(createCategory.getCategoryName());
            return ResponseEntity.status(201).body(
                    GenericResponse.builder()
                            .message("Create Category successfully!")
                            .result(categoryRepository.save(category))
                            .statusCode(HttpStatus.CREATED.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Create Category failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return ResponseEntity.ok().body(
                    GenericResponse.builder()
                            .message("Get All Category Successfully!")
                            .result(categories)
                            .statusCode(HttpStatus.OK.value())
                            .success(true)
                            .build()
            );
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(
                    GenericResponse.builder()
                            .message("Get All Category failed!!!")
                            .result(null)
                            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .success(false)
                            .build()
            );
        }
    }
}
