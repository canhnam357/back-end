package com.bookstore.Controller.General;

import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll () {
        //System.err.println("Call getAll Category in General Controller");
        return categoryService.getAll();
    }
}
