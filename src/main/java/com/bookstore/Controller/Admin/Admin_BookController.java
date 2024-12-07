package com.bookstore.Controller.Admin;

import com.bookstore.DTO.CreateBook;
import com.bookstore.DTO.GenericResponse;
import com.bookstore.Service.BookService;
import com.bookstore.Service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/books")
public class Admin_BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/create")
    public ResponseEntity<GenericResponse> createBook (@RequestBody CreateBook createBook)  {
        return bookService.create(createBook);
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getAll (@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return bookService.getAll(page, size);
    }

    @PostMapping("/upload")
    public ResponseEntity<GenericResponse> uploadImage(@RequestParam("image") MultipartFile file,@RequestParam("bookId") String bookId){
        return bookService.upload(file, bookId);
    }
}
