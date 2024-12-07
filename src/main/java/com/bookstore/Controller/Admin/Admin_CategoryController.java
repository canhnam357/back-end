package com.bookstore.Controller.Admin;

import com.bookstore.DTO.CreateCategory;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/categories")
public class Admin_CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll () {
        return categoryService.getAll();
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createCategory (@RequestBody CreateCategory createCategory)  {
        return categoryService.create(createCategory);
    }
}
