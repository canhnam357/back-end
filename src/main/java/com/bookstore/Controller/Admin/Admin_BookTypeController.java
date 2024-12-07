package com.bookstore.Controller.Admin;

import com.bookstore.DTO.CreateBookType;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookTypeService;
import com.bookstore.Service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/booktypes")
public class Admin_BookTypeController {

    @Autowired
    private BookTypeService bookTypeService;


    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return bookTypeService.getAll(page, size);
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createAuthor (@RequestBody CreateBookType createBookType)  {
        return bookTypeService.create(createBookType);
    }
}
