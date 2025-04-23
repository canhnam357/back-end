package com.bookstore.Service.Impl;

import com.bookstore.DTO.Admin_Req_Create_Category;
import com.bookstore.DTO.Admin_Req_Update_Category;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Entity.Category;
import com.bookstore.Repository.CategoryRepository;
import com.bookstore.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;


    @Override
    public ResponseEntity<GenericResponse> create(Admin_Req_Create_Category createCategory) {
        try {
            Category category = new Category();
            category.setCategoryName(createCategory.getCategoryName());
            categoryRepository.save(category);
            return ResponseEntity.status(201).body(GenericResponse.builder()
                    .message("Create Category successfully!")
                    .statusCode(HttpStatus.CREATED.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Create Category failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> getAll() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return ResponseEntity.ok().body(GenericResponse.builder()
                    .message("Get All Category Successfully!")
                    .result(categories)
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Get All Category failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> delete(String categoryId) {
        try {
            Optional<Category> category = categoryRepository.findById(categoryId);
            if (category.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found category!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            categoryRepository.delete(category.get());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Delete category successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Delete Category failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }

    @Override
    public ResponseEntity<GenericResponse> update(String categoryId, Admin_Req_Update_Category category) {
        try {
            Optional<Category> ele = categoryRepository.findById(categoryId);
            if (ele.isEmpty()) {
                return ResponseEntity.status(404).body(GenericResponse.builder()
                        .message("Not found category!!!")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .success(false)
                        .build());
            }
            ele.get().setCategoryName(category.getCategoryName());
            categoryRepository.save(ele.get());
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .message("Update category successfully!!!")
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(GenericResponse.builder()
                    .message("Update Category failed!!!")
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .build());
        }
    }
}
