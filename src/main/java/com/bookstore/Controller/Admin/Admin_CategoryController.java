package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Category;
import com.bookstore.DTO.Admin_Req_Update_Category;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class Admin_CategoryController {

    private final CategoryService categoryService;

    @GetMapping("") //OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "") String keyword) {
        return categoryService.getAllNotPageable(keyword);
    }

    @PostMapping("") //OK
    public ResponseEntity<GenericResponse> createCategory (@RequestBody Admin_Req_Create_Category createCategory)  {
        return categoryService.create(createCategory);
    }

    @PutMapping("/{categoryId}") // OK
    public ResponseEntity<GenericResponse> updateCategory (@PathVariable String categoryId, @RequestBody Admin_Req_Update_Category category) {
        return categoryService.update(categoryId, category);
    }
}
