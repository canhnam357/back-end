package com.bookstore.Controller.Admin;

import com.bookstore.DTO.Admin_Req_Create_Category;
import com.bookstore.DTO.Admin_Req_Update_Category;
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

    @GetMapping("") //OK
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "") String keyword) {
        System.out.println("ADMIN get all Category");
        return categoryService.getAllNotPageable(keyword);
    }

    @PostMapping("") //OK
    public ResponseEntity<GenericResponse> createCategory (@RequestBody Admin_Req_Create_Category createCategory)  {
        System.out.println("ADMIN create Category");
        return categoryService.create(createCategory);
    }

    @DeleteMapping("") // delete? not
    public ResponseEntity<GenericResponse> deleteCategory (@RequestParam String categoryId) {
        return categoryService.delete(categoryId);
    }

    @PutMapping("/{categoryId}") // OK
    public ResponseEntity<GenericResponse> updateCategory (@PathVariable String categoryId, @RequestBody Admin_Req_Update_Category category) {
        return categoryService.update(categoryId, category);
    }
}
